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
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClazzDetailEnrollStudentView.ARG_NEW_PERSON;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.CUSTOM_FIELD_MIN_UID;

/**
 * The Enrollment person list's presenter for that Clazz.
 * Gets called when Add Student is pressed when within a Clazz.
 *
 */
public class ClazzDetailEnrollStudentPresenter extends
        CommonHandlerPresenter<ClazzDetailEnrollStudentView> {
        //UstadBaseController<ClazzDetailEnrollStudentView> {


    private long currentClazzUid = -1L;
    private UmProvider<PersonWithEnrollment> personWithEnrollmentUmProvider;

    private ClazzMemberDao clazzMemberDao = UmAppDatabase.getInstance(context).getClazzMemberDao();

    private Map<Long, Boolean> enrollmentMap;

    public ClazzDetailEnrollStudentPresenter(Object context, Hashtable arguments,
                                             ClazzDetailEnrollStudentView view) {
        super(context, arguments, view);

        //Set current Clazz being enrolled.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

        enrollmentMap = new HashMap<>();
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        personWithEnrollmentUmProvider = UmAppDatabase.getInstance(context).getClazzMemberDao()
                .findAllPeopleWithEnrollmentForClassUid(currentClazzUid);
        view.setStudentsProvider(personWithEnrollmentUmProvider);

    }

    /**
     * Handles what happens when new Student button clicked. This will:
     * 1. Create a new student.
     * 2. Create null custom fields for the student.
     * 3. Go to PersonEdit for that person.
     *
     */
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
                personFieldDao.findAllCustomFields(CUSTOM_FIELD_MIN_UID,
                        new UmCallback<List<PersonField>>() {
                    @Override
                    public void onSuccess(List<PersonField> allCustomFields) {

                        for (PersonField everyCustomField:allCustomFields){
                            PersonCustomFieldValue cfv = new PersonCustomFieldValue();
                            cfv.setPersonCustomFieldValuePersonCustomFieldUid(
                                    everyCustomField.getPersonCustomFieldUid());
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
                        System.out.println("ClazzDetailEnrollStudentPresenter - " +
                                "findAllCustomFields FAILURE");
                        exception.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                System.out.println("ClazzDetailEnrollStudentPresenter - " +
                        "Inserting new blank student - FAILURE");
                exception.printStackTrace();
            }
        });


    }

    /**
     * Handles what happens when sort order changed from the UI
     * @param order The sort order flag.
     */
    public void handleChangeSortOrder(int order){
        //TODO
    }

    /**
     * Handles what happens when searched for something in the recycler view.
     * @param query The query // string to be searched.
     */
    public void handleClickSearch(String query){
        //TODO
    }

    @Override
    public void handleCommonPressed(Object arg) {
        //Do nothing. We don't want to see Student Details when we are in the enrollment screen.
    }

    @Override
    public void handleSecondaryPressed(Object arg) {
        Map.Entry<PersonWithEnrollment, Boolean> argument =
                (Map.Entry<PersonWithEnrollment, Boolean>) arg;

        handleEnrollChanged(argument.getKey(), argument.getValue());
    }


    public void handleEnrollChanged(PersonWithEnrollment person, boolean enrolled){
        System.out.println("handleEnrollChanged : " + person.getFirstNames() + " " +
            person.getLastName() + " is to be " + enrolled);

        enrollmentMap.put(person.getPersonUid(), enrolled);

        clazzMemberDao.findByPersonUidAndClazzUidAsync(person.getPersonUid(), currentClazzUid,
                new UmCallback<ClazzMember>() {

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
                        newClazzMember.setClazzMemberActive(enrolled);
                        clazzMemberDao.insertAsync(newClazzMember, new UmCallback<Long>() {
                            @Override
                            public void onSuccess(Long result) {
                                newClazzMember.setClazzMemberUid(result);
                            }

                            @Override
                            public void onFailure(Throwable exception) {

                            }
                        });
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

    /**
     * Handles what happens when Done clicked on the list- No need to do anything. Recycler View
     * already made those changes. Just exit the activity (finish).
     */
    public void handleClickDone(){

        view.finish();
    }

    @Override
    public void setUIStrings() {

    }

}
