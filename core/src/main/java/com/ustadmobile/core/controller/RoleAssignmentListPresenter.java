package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.RoleAssignmentListView;
import com.ustadmobile.core.view.RoleAssignmentDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName;

import com.ustadmobile.core.db.dao.EntityRoleDao;

import sun.nio.cs.US_ASCII;

import static com.ustadmobile.core.view.RoleAssignmentDetailView.ENTITYROLE_UID;

/**
 * Presenter for RoleAssignmentList view
 **/
public class RoleAssignmentListPresenter extends UstadBaseController<RoleAssignmentListView> {

    private UmProvider<EntityRoleWithGroupName> umProvider;
    UmAppDatabase repository;
    private EntityRoleDao providerDao;


    public RoleAssignmentListPresenter(Object context, Hashtable arguments, RoleAssignmentListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getEntityRoleDao();


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider 
        umProvider = providerDao.findAllActiveRoleAssignments();
        view.setListProvider(umProvider);

    }

    public void handleEditRoleAssignment(long roleEntityUid){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ENTITYROLE_UID, roleEntityUid);
        impl.go(RoleAssignmentDetailView.VIEW_NAME, args, context);
    }

    public void handleDeleteRoleAssignment(long roleEntityUid){
        providerDao.inavtivateEntityRoleAsync(roleEntityUid, null);
    }

    public void handleClickPrimaryActionButton() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(RoleAssignmentDetailView.VIEW_NAME, args, context);
    }


}
