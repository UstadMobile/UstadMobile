package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.ReportOptionsDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.DashboardEntry;

import com.ustadmobile.core.db.dao.DashboardEntryDao;

/**
 * Presenter for ReportOptionsDetail view
 **/
public class ReportOptionsDetailPresenter extends UstadBaseController<ReportOptionsDetailView> {

    UmAppDatabase repository;
    private DashboardEntryDao dashboardEntryDao;


    public ReportOptionsDetailPresenter(Object context, Hashtable arguments, ReportOptionsDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        dashboardEntryDao = repository.getDashboardEntryDao();


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }


}
