package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView;
import com.ustadmobile.core.view.PersonEditView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.Hashtable;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;

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
        personDao.insertAsync(newPerson, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                Hashtable args = new Hashtable();
                args.put(ARG_CLAZZ_UID, currentClazzUid);
                args.put(ARG_PERSON_UID, result);
                impl.go(PersonEditView.VIEW_NAME, args, view.getContext());
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

    //TODO check arg needs more
    public void handleEnrollChanged(PersonWithEnrollment person, boolean enrolled){
        //TODO



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
