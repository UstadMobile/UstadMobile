package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView;
import com.ustadmobile.core.view.ClazzStudentListView;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;
import com.ustadmobile.lib.db.entities.Role;

import java.util.ArrayList;
import java.util.Hashtable;

import static com.ustadmobile.core.view.ClazzDetailEnrollStudentView.ARG_NEW_PERSON_TYPE;
import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_ATTENDANCE_ASC;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_ATTENDANCE_DESC;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_NAME_ASC;
import static com.ustadmobile.core.view.ClazzListView.SORT_ORDER_NAME_DESC;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;


/**
 * The Presenter/Controller for ClazzStudentListFragment. This is responsible for the logic of
 * populating the Student list for the Class Selected and other actions associated with it like
 * navigating to student detail view and adding/enrolling a new student.
 *
 */
public class ClazzStudentListPresenter extends
        CommonHandlerPresenter<ClazzStudentListView>{

    private long currentClazzId = -1L;
    private UmProvider<PersonWithEnrollment> clazzPersonListProvider;

    private Hashtable<Long, Integer> idToOrderInteger;

    private boolean teachersEditable = false;

    private boolean canAddTeachers = false;
    private boolean canAddStudents = false;

    public boolean isCanAddTeachers() {
        return canAddTeachers;
    }

    public void setCanAddTeachers(boolean canAddTeachers) {
        this.canAddTeachers = canAddTeachers;
    }

    public boolean isCanAddStudents() {
        return canAddStudents;
    }

    public void setCanAddStudents(boolean canAddStudents) {
        this.canAddStudents = canAddStudents;
    }

    private long loggedInPerson = 0L;

    public boolean isTeachersEditable() {
        return teachersEditable;
    }

    public void setTeachersEditable(boolean teachersEditable) {
        this.teachersEditable = teachersEditable;
    }

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public ClazzStudentListPresenter(Object context,
                                     Hashtable arguments,
                                     ClazzStudentListView view) {
        super(context, arguments, view);

        //Get Clazz Uid from argument and set it here to the Presenter
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzId = (long) arguments.get(ARG_CLAZZ_UID);
        }

        loggedInPerson = UmAccountManager.getActiveAccount(context).getPersonUid();
    }


    /**
     * The Presenter here's onCreate. In Order:
     *      1. This populates the provider and sets it to the View.
     *
     * This will be called when the implementation View is ready.
     * (ie: on Android, this is called in the Fragment's onCreateView() )
     *
     * @param savedState    The saved state
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        this.teachersEditable = true;

        //Find the Provider
        clazzPersonListProvider = repository.getClazzMemberDao()
                .findAllPersonWithEnrollmentInClazzByClazzUid(currentClazzId);
        setProviderToView();

        //Initialise sort spinner data:
        idToOrderInteger = new Hashtable<>();
        updateSortSpinnerPreset();

        checkPermissions();
    }

    public void checkPermissions(){
        PersonDao personDao = repository.getPersonDao();
        ClazzDao clazzDao = repository.getClazzDao();
        clazzDao.personHasPermission(loggedInPerson, Role.PERMISSION_PERSON_INSERT,
            new UmCallbackWithDefaultValue<>(false, new UmCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    canAddStudents = result;
                    canAddTeachers = result;
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            }));
    }

    /**
     * Common method to convert Array List to String Array
     *
     * @param presetAL The array list of string type
     * @return  String array
     */
    private String[] arrayListToStringArray(ArrayList<String> presetAL){
        Object[] objectArr = presetAL.toArray();
        String[] strArr = new String[objectArr.length];
        for(int j = 0 ; j < objectArr.length ; j ++){
            strArr[j] = (String) objectArr[j];
        }
        return strArr;
    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private void updateSortSpinnerPreset(){
        ArrayList<String> presetAL = new ArrayList<>();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        idToOrderInteger = new Hashtable<>();

        presetAL.add(impl.getString(MessageID.namesb, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_NAME_ASC);
        presetAL.add(impl.getString(MessageID.attendance_high_to_low, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_ATTENDANCE_DESC);
        presetAL.add(impl.getString(MessageID.attendance_low_to_high, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_ORDER_ATTENDANCE_ASC);

        String[] sortPresets = arrayListToStringArray(presetAL);

        view.updateSortSpinner(sortPresets);
    }


    /**
     * Sets the provider set to this Presenter to the view.
     */
    private void setProviderToView(){
        view.setPersonWithEnrollmentProvider(clazzPersonListProvider);
    }

    /**
     * Method logic for what happens when you click Add Student
     *
     */
    public void goToAddStudentFragment(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzId);
        args.put(ARG_NEW_PERSON_TYPE, ClazzMember.ROLE_STUDENT);
        impl.go(ClazzDetailEnrollStudentView.VIEW_NAME, args, view.getContext());
    }

    /**
     * Method logic for what happens when you click Add Teacher
     *
     */
    private void handleAddTeacher(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzId);
        args.put(ARG_NEW_PERSON_TYPE, ClazzMember.ROLE_TEACHER);
        impl.go(ClazzDetailEnrollStudentView.VIEW_NAME, args, view.getContext());
    }

    /**
     * This method handles what happens when a student is clicked - It goes to the PersonDetailView
     * where it shows all information about the student/person.
     *
     * @param personUid Ths student Person UID.
     */
    private void handleClickStudent(long personUid){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzId);
        args.put(ARG_PERSON_UID, personUid);
        impl.go(PersonDetailView.VIEW_NAME, args, view.getContext());
    }

    /**
     * Method logic for what happens when we change the order of the student list.
     *
     * @param order The order flag. 0 to Sort by Name, 1 to Sort by Attendance, 2 to Sort by date
     */
    public void handleChangeSortOrder(long order){
        order=order+1;

        if(idToOrderInteger.containsKey(order)){
            int sortCode = idToOrderInteger.get(order);
            getAndSetProvider(sortCode);
        }
    }

    /**
     * This method updates the Class List provider based on the sort order flag selected.
     * Every order has a corresponding order by change in the database query where this method
     * reloads the class list provider.
     *
     * @param order The order selected.
     */
    private void getAndSetProvider(int order){
        switch (order){
            case SORT_ORDER_NAME_ASC:
                clazzPersonListProvider =
                        repository.getClazzMemberDao()
                        .findAllPersonWithEnrollmentInClazzByClazzUidSortByNameAsc(currentClazzId);
                break;
            case SORT_ORDER_NAME_DESC:
                clazzPersonListProvider =
                        repository.getClazzMemberDao()
                        .findAllPersonWithEnrollmentInClazzByClazzUidSortByNameDesc(currentClazzId);
                break;
            case SORT_ORDER_ATTENDANCE_ASC:
                clazzPersonListProvider =
                        repository.getClazzMemberDao()
                        .findAllPersonWithEnrollmentInClazzByClazzUidSortByAttendanceAsc(currentClazzId);
                break;
            case SORT_ORDER_ATTENDANCE_DESC:
                clazzPersonListProvider =
                        repository.getClazzMemberDao()
                        .findAllPersonWithEnrollmentInClazzByClazzUidSortByAttendanceDesc(currentClazzId);
                break;
            default:
                clazzPersonListProvider =
                        repository.getClazzMemberDao()
                        .findAllPersonWithEnrollmentInClazzByClazzUidSortByNameAsc(currentClazzId);
                break;
        }

        updateProviderToView();

    }

    private void updateProviderToView(){
        view.setPersonWithEnrollmentProvider(clazzPersonListProvider);
    }

    /**
     * This is the the primary action button for the list of students in this screen. It calls
     * handleClickStudent which in turn goes to PersonDetailView.
     *
     * @param arg The argument passed to the primary handler which is the Person's Uid.
     */
    @Override
    public void handleCommonPressed(Object arg) {

        if((Long)arg == 0){
            goToAddStudentFragment();
        }else {
            handleClickStudent((Long) arg);
        }
    }

    /**
     * The secondary action handler for the student list recycler adapter. There is no secondary
     * action on the list of students yet. For now, this does nothing.
     * @param arg   The argument passed to the secondary action handler of every item in the
     *              recycler view.
     */
    @Override
    public void handleSecondaryPressed(Object arg) {
        //No secondary action here.
        if((Long)arg < 0){
            handleAddTeacher();
        }
    }

}
