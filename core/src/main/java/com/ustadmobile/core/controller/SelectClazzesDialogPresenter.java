package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.view.ReportEditView;
import com.ustadmobile.core.view.SelectClazzesDialogView;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.view.ReportEditView.ARG_CLASSES_SET;


/**
 * The SelectClazzesDialog Presenter.
 */
public class SelectClazzesDialogPresenter
        extends UstadBaseController<SelectClazzesDialogView> {

    //Any arguments stored as variables here
    private UmProvider<ClazzWithNumStudents> clazzWithEnrollmentUmProvider;
    private List<Long> locations;
    private HashMap<String, Long> clazzes;
    private List<Long> selectedClazzesList;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public SelectClazzesDialogPresenter(Object context, Hashtable arguments,
                                        SelectClazzesDialogView view) {
        super(context, arguments, view);

        System.out.println("hey hey");
        if(arguments.containsKey(ReportEditView.ARG_LOCATIONS_SET)){
            locations = (ArrayList<Long>) arguments.get(ReportEditView.ARG_LOCATIONS_SET);
        }

        System.out.println("Hey 2");
        if(arguments.containsKey(ARG_CLASSES_SET)){
            long[] clazzesSelected = (long[]) arguments.get(ARG_CLASSES_SET);
            selectedClazzesList =
                    ReportOverallAttendancePresenter.convertLongArray(clazzesSelected);
        }

        System.out.println("Hey 3");
    }



    public HashMap<String, Long> getClazzes() {
        return clazzes;
    }

    public void setClazzes(HashMap<String, Long> clazzes) {
        this.clazzes = clazzes;
    }

    public void addToClazzes(Clazz clazzUid){
        if(!clazzes.containsKey(clazzUid.getClazzName())){
            clazzes.put(clazzUid.getClazzName(), clazzUid.getClazzUid());
        }
    }

    public void removeFromClazzes(Clazz clazzUid){
        if(clazzes.containsKey(clazzUid.getClazzName())){
            clazzes.remove(clazzUid.getClazzName());
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        clazzes = new HashMap<>();

        //Find the provider
        clazzWithEnrollmentUmProvider = repository.getClazzDao()
                .findAllClazzes();
        view.setClazzListProvider(clazzWithEnrollmentUmProvider);

    }

    @Override
    public void setUIStrings() {

    }

    public void handleCommonPressed(Object arg) {
        // The finish() should call the onResult method in parent activity, etc.
        // Make sure you send the list
        view.finish();
    }


    public List<Long> getSelectedClazzesList() {
        return selectedClazzesList;
    }

    public void setSelectedClazzesList(List<Long> selectedClazzesList) {
        this.selectedClazzesList = selectedClazzesList;
    }
}
