package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.AuditLogSelectionView;
import com.ustadmobile.core.view.AuditLogListView;


/**
 * Presenter for AuditLogSelection view
 **/
public class AuditLogSelectionPresenter extends UstadBaseController<AuditLogSelectionView> {

    UmAppDatabase repository;


    public AuditLogSelectionPresenter(Object context, Hashtable arguments, AuditLogSelectionView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }

    public void handleClickPrimaryActionButton() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(AuditLogListView.VIEW_NAME, args, context);
    }


}
