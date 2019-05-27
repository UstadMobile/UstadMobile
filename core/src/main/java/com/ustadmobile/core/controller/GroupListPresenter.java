package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.GroupListView;
import com.ustadmobile.core.view.GroupDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.GroupWithMemberCount;
import com.ustadmobile.lib.db.entities.PersonGroup;

import com.ustadmobile.core.db.dao.PersonGroupDao;

import static com.ustadmobile.core.view.GroupDetailView.GROUP_UID;

/**
 * Presenter for GroupList view
 **/
public class GroupListPresenter extends UstadBaseController<GroupListView> {

    private UmProvider<GroupWithMemberCount> umProvider;
    UmAppDatabase repository;
    private PersonGroupDao providerDao;


    public GroupListPresenter(Object context, Hashtable arguments, GroupListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getPersonGroupDao();


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider 
        umProvider = providerDao.findAllActiveGroupsWithoutIndividualGroup();
        view.setListProvider(umProvider);

    }

    public void handleEditGroup(long uid){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(GROUP_UID, uid);
        impl.go(GroupDetailView.VIEW_NAME, args, context);
    }

    public void handleDeleteGroup(long uid){
        providerDao.inactivateGroupAsync(uid,null);
    }

    public void handleClickPrimaryActionButton() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(GroupDetailView.VIEW_NAME, args, context);
    }


}
