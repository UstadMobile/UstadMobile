package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView;
import com.ustadmobile.core.view.PersonEditView;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.core.view.ClazzDetailEnrollStudentView.ARG_NEW_PERSON_TYPE;
import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClazzDetailEnrollStudentView.ARG_NEW_PERSON;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.CUSTOM_FIELD_MIN_UID;

/**
 * The ClazzDetailEnrollStudent's presenter - responsible for the logic of Enrolling a student
 * Enrollment detail screen shows all students that are enrolled as well as students not enrolled
 * along with an enrollment tick mark.
 *
 * Gets called when Add Student is pressed when within a Clazz.
 *
 */
public class ClazzDetailEnrollStudentPresenter extends
        CommonHandlerPresenter<ClazzDetailEnrollStudentView> {

    private long currentClazzUid = -1L;
    private int currentRole = -1;
    private UmProvider<PersonWithEnrollment> personWithEnrollmentUmProvider;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);
    private ClazzMemberDao clazzMemberDao = repository.getClazzMemberDao();

    public ClazzDetailEnrollStudentPresenter(Object context, Hashtable arguments,
                                             ClazzDetailEnrollStudentView view) {
        super(context, arguments, view);

        //Set current Clazz being enrolled.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

        if(arguments.containsKey(ARG_NEW_PERSON_TYPE)){
            currentRole = (int) arguments.get(ARG_NEW_PERSON_TYPE);
        }

    }

    /**
     * Order:
     *      1. Gets all students with enrollment information from the database.
     *      2. Sets the provider to the view.
     *
     * @param savedState The savedState
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(currentRole == ClazzMember.ROLE_TEACHER){
            personWithEnrollmentUmProvider = repository.getClazzMemberDao()
                    .findAllEligibleTeachersWithEnrollmentForClassUid(currentClazzUid);
        }else{
            personWithEnrollmentUmProvider = repository.getClazzMemberDao()
                    .findAllPeopleWithEnrollmentForClassUid(currentClazzUid);
        }

        setProviderToView();

    }

    /**
     * Sets the provider attached to this Presenter to the View.
     */
    private void setProviderToView(){
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
        PersonDao personDao = repository.getPersonDao();
        PersonCustomFieldDao personFieldDao =
                repository.getPersonCustomFieldDao();
        PersonCustomFieldValueDao customFieldValueDao =
                repository.getPersonCustomFieldValueDao();

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

                        Hashtable<String, Object> args = new Hashtable<>();
                        args.put(ARG_CLAZZ_UID, currentClazzUid);
                        args.put(ARG_PERSON_UID, result);
                        args.put(ARG_NEW_PERSON_TYPE, currentRole);
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
     * Does nothing. Any common handler goes here. Here we are doing nothing. We don't want to see Student Details
     * when we are in the enrollment screen.
     *
     * @param arg   Any argument to the handler.
     */
    @Override
    public void handleCommonPressed(Object arg) {
    }

    /**
     * The secondary handler for the Enrollment screen on the main recycler view - is to toggle
     * enrollment for that student pressed.
     *
     * @param arg The argument here - is a Map of the student id and the enrollment status
     */
    @Override
    public void handleSecondaryPressed(Object arg) {

        //The Unchecked cast warning is expected. We are making a personal assumption from the View.
        Map.Entry<PersonWithEnrollment, Boolean> argument =
                (Map.Entry<PersonWithEnrollment, Boolean>) arg;

        handleEnrollChanged(argument.getKey(), argument.getValue());
    }

    /**
     * Handles role changed for every person. This method will update the clazzMember for the
     * person whose role changed. If the student person does not have a Clazz Member entry, it will
     * create one and persist the database with the new value.
     *
     * @param person The person with Enrollment object whose to be enrolled or not.
     * @param enrolled  The enrolled status. True for enrolled, False for un-enrolled.
     */
    private void handleEnrollChanged(PersonWithEnrollment person, boolean enrolled){
        System.out.println("handleEnrollChanged : " + person.getFirstNames() + " " +
            person.getLastName() + " is to be " + enrolled);

        clazzMemberDao.findByPersonUidAndClazzUidAsync(person.getPersonUid(), currentClazzUid,
                new UmCallback<ClazzMember>() {

            @Override
            public void onSuccess(ClazzMember existingClazzMember) {

                if (enrolled){
                    if(existingClazzMember == null){
                        //Create the ClazzMember
                        ClazzMember newClazzMember = new ClazzMember();
                        newClazzMember.setClazzMemberClazzUid(currentClazzUid);
                        if(currentRole == ClazzMember.ROLE_TEACHER){
                            newClazzMember.setRole(ClazzMember.ROLE_TEACHER);
                        }else {
                            newClazzMember.setRole(ClazzMember.ROLE_STUDENT);
                        }
                        newClazzMember.setClazzMemberPersonUid(person.getPersonUid());
                        newClazzMember.setDateJoined(System.currentTimeMillis());
                        newClazzMember.setClazzMemberActive(true);
                        clazzMemberDao.insertAsync(newClazzMember, new UmCallback<Long>() {
                            @Override
                            public void onSuccess(Long result) {
                                newClazzMember.setClazzMemberUid(result);
                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                exception.printStackTrace();
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
