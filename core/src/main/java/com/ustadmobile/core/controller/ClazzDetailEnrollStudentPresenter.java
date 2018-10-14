package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView;
import com.ustadmobile.core.view.PersonEditView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClazzDetailEnrollStudentView.ARG_NEW_PERSON;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.CUSTOM_FIELD_MIN_UID;

public class ClazzDetailEnrollStudentPresenter extends
        UstadBaseController<ClazzDetailEnrollStudentView> {


    private long currentClazzUid = -1L;
    private UmProvider<PersonWithEnrollment> personWithEnrollmentUmProvider;

    ClazzMemberDao clazzMemberDao = UmAppDatabase.getInstance(context).getClazzMemberDao();


    public ClazzDetailEnrollStudentPresenter(Object context, Hashtable arguments,
                                             ClazzDetailEnrollStudentView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        personWithEnrollmentUmProvider = UmAppDatabase.getInstance(context).getClazzMemberDao()
                .findAllPeopleWithEnrollmentForClassUid(currentClazzUid);
        view.setStudentsProvider(personWithEnrollmentUmProvider);

    }

    public void handleClickEnrollNewStudent(){
        //Goes to PersonEditActivity with currentClazzUid passed as argument
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Person newPerson = new Person();
        PersonDao personDao = UmAppDatabase.getInstance(context).getPersonDao();
        PersonCustomFieldDao personFieldDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldDao();
        PersonCustomFieldValueDao customFieldValueDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldValueDao();

        personDao.insertAsync(newPerson, new UmCallback<Long>() {

            @Override
            public void onSuccess(Long result) {

                //Also create null Custom Field values so it shows up in the Edit screen.
                personFieldDao.findAllCustomFields(CUSTOM_FIELD_MIN_UID, new UmCallback<List<PersonField>>() {
                    @Override
                    public void onSuccess(List<PersonField> allCustomFields) {

                        for (PersonField everyCustomField:allCustomFields){
                            PersonCustomFieldValue cfv = new PersonCustomFieldValue();
                            cfv.setPersonCustomFieldValuePersonCustomFieldUid(everyCustomField.getPersonCustomFieldUid());
                            cfv.setPersonCustomFieldValuePersonUid(result);
                            cfv.setPersonCustomFieldValueUid(customFieldValueDao.insert(cfv));
                        }

                        Hashtable args = new Hashtable();
                        args.put(ARG_CLAZZ_UID, currentClazzUid);
                        args.put(ARG_PERSON_UID, result);
                        args.put(ARG_NEW_PERSON, "true");
                        impl.go(PersonEditView.VIEW_NAME, args, view.getContext());
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });


            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });


    }

    public void handleChangeSortOrder(int order){
        //TODO
    }

    public void handleClickSearch(String query){
        //TODO
    }

    public void handleEnrollChanged(PersonWithEnrollment person, boolean enrolled){

        clazzMemberDao.findByPersonUidAndClazzUidAsync(person.getPersonUid(), currentClazzUid, new UmCallback<ClazzMember>() {
            @Override
            public void onSuccess(ClazzMember existingClazzMember) {

                if (enrolled){
                    if(existingClazzMember == null){
                        //Create the ClazzMember
                        ClazzMember newClazzMember = new ClazzMember();
                        newClazzMember.setClazzMemberClazzUid(currentClazzUid);
                        newClazzMember.setRole(ClazzMember.ROLE_STUDENT);
                        newClazzMember.setClazzMemberPersonUid(person.getPersonUid());
                        newClazzMember.setDateJoined(System.currentTimeMillis());
                        newClazzMember.setClazzMemberUid(clazzMemberDao.insert(newClazzMember));
                    }else {
                        if (!existingClazzMember.isClazzMemberActive()){
                            existingClazzMember.setClazzMemberActive(true);
                            clazzMemberDao.update(existingClazzMember);
                        }
                        //else let it be
                    }

                }else{
                    //if already enrolled, disable ClazzMember.
                    if(existingClazzMember != null){
                        existingClazzMember.setClazzMemberActive(false);
                        clazzMemberDao.update(existingClazzMember);
                    }
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });


    }

    public void handleClickDone(){
        view.finish();
    }

    @Override
    public void setUIStrings() {

    }
}
