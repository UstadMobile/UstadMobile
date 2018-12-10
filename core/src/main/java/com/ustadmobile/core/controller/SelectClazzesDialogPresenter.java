package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.view.ReportEditView;
import com.ustadmobile.core.view.SelectClazzesDialogView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;


/**
 * The SelectClazzesDialog Presenter.
 */
public class SelectClazzesDialogPresenter
        extends CommonHandlerPresenter<SelectClazzesDialogView> {

    //Any arguments stored as variables here
    private UmProvider<ClazzWithNumStudents> clazzWithEnrollmentUmProvider;
    private List<Long> locations;
    private HashMap<String, Long> clazzes;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public SelectClazzesDialogPresenter(Object context, Hashtable arguments,
                                        SelectClazzesDialogView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ReportEditView.ARG_LOCATIONS_SET)){
            locations = (ArrayList<Long>) arguments.get(ReportEditView.ARG_LOCATIONS_SET);
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
        clazzWithEnrollmentUmProvider = repository.getClazzDao()
                .findAllClazzes();
        view.setClazzListProvider(clazzWithEnrollmentUmProvider);

    }

    @Override
    public void setUIStrings() {

    }

    @Override
    public void handleCommonPressed(Object arg) {

        //TODO: Check if nothing else required.
        // The finish() should call the onResult method in parent activity, etc.
        // Make sure you send the list
        view.finish();
    }

    @Override
    public void handleSecondaryPressed(Object arg) {

    }
}
