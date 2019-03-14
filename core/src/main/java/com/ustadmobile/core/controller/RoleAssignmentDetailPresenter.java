package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.RoleAssignmentDetailView;


/**
 * Presenter for RoleAssignmentDetail view
 **/
public class RoleAssignmentDetailPresenter extends UstadBaseController<RoleAssignmentDetailView> {

    UmAppDatabase repository;


    public RoleAssignmentDetailPresenter(Object context, Hashtable arguments, RoleAssignmentDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }


    public void handleClickDone() {

        view.finish();
    }
}
