package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.AuditLogDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.view.AuditLogListView;
import com.ustadmobile.lib.db.entities.AuditLog;

import java.util.Hashtable;

/**
 * Presenter for AuditLogList view
 **/
public class AuditLogListPresenter extends UstadBaseController<AuditLogListView> {

    private UmProvider<AuditLog> umProvider;
    UmAppDatabase repository;
    private AuditLogDao providerDao;


    public AuditLogListPresenter(Object context, Hashtable arguments, AuditLogListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getAuditLogDao();

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider 
        umProvider = providerDao.findAllAuditLogs();
        view.setListProvider(umProvider);

    }


    public void handleClickDone() {

        view.finish();
    }
}
