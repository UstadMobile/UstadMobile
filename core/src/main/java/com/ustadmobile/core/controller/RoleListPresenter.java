package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.RoleListView;
import com.ustadmobile.core.view.RoleDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Role;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.RoleDao;

/**
 * Presenter for RoleList view
 **/
public class RoleListPresenter extends UstadBaseController<RoleListView> {

    private UmProvider<Role> umProvider;
    UmAppDatabase repository;
    private RoleDao providerDao;


    public RoleListPresenter(Object context, Hashtable arguments, RoleListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getRoleDao();


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider 
        umProvider = providerDao.findAllRoles();
        view.setListProvider(umProvider);

    }

    public void handleClickPrimaryActionButton() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(RoleDetailView.VIEW_NAME, args, context);
    }


}
