package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.RoleDetailView;


/**
 * Presenter for RoleDetail view
 **/
public class RoleDetailPresenter extends UstadBaseController<RoleDetailView> {

    UmAppDatabase repository;


    public RoleDetailPresenter(Object context, Hashtable arguments, RoleDetailView view) {
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
