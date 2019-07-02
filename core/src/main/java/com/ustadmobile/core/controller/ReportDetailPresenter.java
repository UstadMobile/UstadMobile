package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.DashboardEntryDao;
import com.ustadmobile.core.view.ReportDetailView;
import com.ustadmobile.core.view.UstadView;

import java.util.Hashtable;

/**
 * Presenter for ReportDetailView view - common for every report view.
 **/
public abstract class ReportDetailPresenter<V extends ReportDetailView>
        extends UstadBaseController<V>  {

    UmAppDatabase repository;

    public ReportDetailPresenter(Object context) {
        super(context);
    }

    //The constructor will throw an uncast check warning. That is expected.
    public ReportDetailPresenter(Object context, Hashtable arguments, UstadView view) {
        super(context, arguments, (V) view);
    }

    public abstract void handleClickAddToDashboard();

    public abstract void handleClickEditReport();

    public abstract void handleClickDownloadReport();

}
