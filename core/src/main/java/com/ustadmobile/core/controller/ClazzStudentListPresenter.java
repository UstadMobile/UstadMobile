package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView;
import com.ustadmobile.core.view.ClazzStudentListView;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
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


    public ClazzStudentListPresenter(Object context,
                                     Hashtable arguments,
                                     ClazzStudentListView view) {
        super(context, arguments, view);

        //Get Clazz Uid from argument and set it here to the Presenter
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzId = (long) arguments.get(ARG_CLAZZ_UID);
        }
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

        clazzPersonListProvider = UmAppDatabase.getInstance(context).getClazzMemberDao()
                .findAllPersonWithEnrollmentInClazzByClazzUid(currentClazzId);
        setProviderToView();
    }

    /**
     * Sets the provider set to this Presenter to the view.
     */
    private void setProviderToView(){
        view.setPersonWithEnrollmentProvider(clazzPersonListProvider);
    }

    /**
     * Method logic for what happens when you click the FAB Add Student
     *
     */
    public void goToAddStudentFragment(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzId);
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

//    /**
//     * Method logic for what happens when we change the order of the student list.
//     *
//     * @param order The order flag. 0 to Sort by Name, 1 to Sort by Attendance, 2 to Sort by date
//     */
//    public void handleChangeSortOrder(int order){
//        //TODO: Change provider's sort order
//    }


    /**
     * This is the the primary action button for the list of students in this screen. It calls
     * handleClickStudent which in turn goes to PersonDetailView.
     *
     * @param arg The argument passed to the primary handler which is the Person's Uid.
     */
    @Override
    public void handleCommonPressed(Object arg) {
        handleClickStudent((Long)arg);
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
    }

    /**
     * UstadBaseController's setUiString().
     *
     * Right not it doesn't do anything.
     */
    @Override
    public void setUIStrings() {

    }
}
