package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClazzStudentListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;

import java.util.Hashtable;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;


/**
 * The Presenter/Controller for ClazzStudentListFragment. This is responsible in creating the
 * provider from the Dao and assigning it to the View.
 */
public class ClazzStudentListPresenter extends UstadBaseController<ClazzStudentListView> {

    private long currentClazzId = -1L;



    /**
     * Constructor to the ClassStudentsList Presenter.
     * We get the class uid from the arguments passed to it.
     *(TODO: Check if true)
     *
     * @param context
     * @param arguments
     * @param view
     */
    public ClazzStudentListPresenter(Object context,
                                     Hashtable arguments,
                                     ClazzStudentListView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzId = (long) arguments.get(ARG_CLAZZ_UID);
        }
    }

    /**
     * UstadBaseController's setUiString().
     *
     * Right not it doesn't do anything.
     */
    @Override
    public void setUIStrings() {

    }

    private UmProvider<ClazzMemberWithPerson> clazzStudentListProvider;

    /**
     * The Presenter here's onCreate. This populates the provider and sets it to the View.
     *
     * This will be called when the implementation View is ready.
     * (ie: on Android, this is called in the Fragment's onCreateView() )
     *
     * @param savedState
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        clazzStudentListProvider = UmAppDatabase.getInstance(context).getClazzMemberDao()
                .findClazzMembersByClazzId(currentClazzId);
        view.setClazzMembersProvider(clazzStudentListProvider);
    }

    /**
     * Method logic for what happens when you click the FAB Add Student
     *
     * @param clazz The current Clazz that we are working on.
     */
    public void goToAddStudentFragment(Clazz clazz){
        //TODO: Go To Add new student activity/fragment for that Class
    }

    /**
     * Method logic for what happens when we change the order of the student list.
     *
     * @param order The order flag. 0 to Sort by Name, 1 to Sort by Attendance, 2 to Sort by date
     */
    public void handleChangeSortOrder(int order){

        //TODO: Change provider's sort order
    }


}
