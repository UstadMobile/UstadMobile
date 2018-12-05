package com.ustadmobile.core.controller;

import java.util.ArrayList;
import java.util.Hashtable;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.view.ReportEditView;
import com.ustadmobile.core.view.SelectClazzesDialogView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;


/**
 * The SelectClazzesDialog Presenter.
 */
public class SelectClazzesDialogPresenter
        extends CommonHandlerPresenter<SelectClazzesDialogView> {

    //Any arguments stored as variables here
    private UmProvider<ClazzWithNumStudents> clazzWithEnrollmentUmProvider;
    private ArrayList<Long> locations;
    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public SelectClazzesDialogPresenter(Object context, Hashtable arguments,
                                        SelectClazzesDialogView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        //eg: if(arguments.containsKey(ARG_CLAZZ_UID)){
        //    currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        //}
        if(arguments.containsKey(ReportEditView.ARG_LOCATIONS_SET)){
            locations = (ArrayList<Long>) arguments.get(ReportEditView.ARG_LOCATIONS_SET);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


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
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Create arguments
        Hashtable args = new Hashtable();
        //eg: args.put(ARG_CLAZZ_UID, selectedObjectUid);

        //Go to view
        //eg: impl.go(SELEditView.VIEW_NAME, args, view.getContext());
    }

    @Override
    public void handleSecondaryPressed(Object arg) {

    }
}
