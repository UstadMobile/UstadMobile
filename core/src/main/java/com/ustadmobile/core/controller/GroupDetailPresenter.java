package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.GroupDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.PersonGroupMember;

import com.ustadmobile.core.db.dao.PersonGroupMemberDao;

/**
 * Presenter for GroupDetail view
 **/
public class GroupDetailPresenter extends UstadBaseController<GroupDetailView> {

    private UmProvider<PersonGroupMember> umProvider;
    UmAppDatabase repository;
    private PersonGroupMemberDao providerDao;
    private long currentGroupUid = 0;


    public GroupDetailPresenter(Object context, Hashtable arguments, GroupDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getPersonGroupMemberDao();

        //Get or create group Uid



    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider 
        umProvider = providerDao.finAllMembersWithGroupId(currentGroupUid);
        view.setListProvider(umProvider);

    }


    public void handleClickDone() {

        view.finish();
    }
}
