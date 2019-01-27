package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.CallPersonRelatedDialogView;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;
import com.ustadmobile.lib.db.entities.Person;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;

public class CallPersonRelatedDialogPresenter extends UstadBaseController<CallPersonRelatedDialogView> {


    Person currentPerson;
    long personUid;
    long clazzUid;
    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);
    public static final int NUMBER_FATHER = 1;
    public static final int NUMBER_MOTHER = 2;
    public static final int NUMBER_TEACHR = 3;
    public static final int NUMBER_RETENTION_OFFICER = 4;


    public class NameWithNumber{
        public String name, number;
        NameWithNumber(String name, String number){
            this.name = name;
            this.number = number;
        }
    }

    LinkedHashMap<Integer, NameWithNumber> putThisMap;

    public CallPersonRelatedDialogPresenter(Object context, Hashtable arguments, CallPersonRelatedDialogView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARG_PERSON_UID)){
            personUid = (long) arguments.get(ARG_PERSON_UID);
        }else{
            personUid = 0;
        }
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            clazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }else{
            clazzUid = 0;
        }
    }

    public void generateCallingMap(Person personToCall, ClazzMemberWithPerson teacherToCall){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        putThisMap = new LinkedHashMap<>();
        putThisMap.put(NUMBER_FATHER, new NameWithNumber(impl.getString(MessageID.father, context) +
                "(" + personToCall.getFatherName() + ")",personToCall.getFatherNumber()));
        putThisMap.put(NUMBER_MOTHER, new NameWithNumber(impl.getString(MessageID.mother, context) +
                "(" + personToCall.getMotherName() + ")",personToCall.getMotherNum()));
        putThisMap.put(NUMBER_TEACHR, new NameWithNumber(impl.getString(MessageID.teacher, context) +
                "(" + teacherToCall.getPerson().getFirstNames() + " " +
                teacherToCall.getPerson().getLastName()+ ")",
                teacherToCall.getPerson().getPhoneNum()));


        //TODO: Retention officer's number? How?
//        callMeMap.put(impl.getString(MessageID.retention_officer, context) +
//                "(" + ")", );


        view.setOnDisplay(putThisMap);
    }

    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        PersonDao personDao = repository.getPersonDao();
        ClazzDao clazzDao = repository.getClazzDao();
        ClazzMemberDao clazzMemberDao = repository.getClazzMemberDao();

        personDao.findByUidAsync(personUid, new UmCallback<Person>() {
            @Override
            public void onSuccess(Person result) {
                if(result!=null){
                    currentPerson = result;

                    clazzMemberDao.findClazzMemberWithPersonByRoleForClazzUid(clazzUid,
                            ClazzMember.ROLE_TEACHER, new UmCallback<List<ClazzMemberWithPerson>>() {
                                @Override
                                public void onSuccess(List<ClazzMemberWithPerson> result) {
                                    ClazzMemberWithPerson mainTeacher;
                                    if(!result.isEmpty()) {
                                        mainTeacher = result.get(0);

                                        generateCallingMap(currentPerson, mainTeacher);
                                    }
                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    exception.printStackTrace();
                                }
                            });
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

    }

    @Override
    public void setUIStrings() {

    }
}
