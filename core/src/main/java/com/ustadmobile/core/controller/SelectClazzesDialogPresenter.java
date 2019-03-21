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
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATIONS_SET;


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
        
        if(arguments.containsKey(ARG_LOCATIONS_SET)){
            long[] locationsArray = (long[]) arguments.get(ARG_LOCATIONS_SET);
            locations =
                    ReportOverallAttendancePresenter.convertLongArray(locationsArray);
        }

        if(arguments.containsKey(ARG_CLASSES_SET)){
            long[] clazzesSelected = (long[]) arguments.get(ARG_CLASSES_SET);
            selectedClazzesList =
                    ReportOverallAttendancePresenter.convertLongArray(clazzesSelected);
        }

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
        if(locations != null && !locations.isEmpty()){
            clazzWithEnrollmentUmProvider = repository.getClazzDao()
                    .findAllClazzesInLocationList(locations);
        }else {
            clazzWithEnrollmentUmProvider = repository.getClazzDao()
                    .findAllClazzes();
        }
        view.setClazzListProvider(clazzWithEnrollmentUmProvider);

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
