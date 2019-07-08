package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.DashboardEntryDao;
import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.ReportOptions;
import com.ustadmobile.core.view.ReportDetailView;
import com.ustadmobile.core.view.ReportOptionsDetailView;
import com.ustadmobile.lib.db.entities.DashboardEntry;
import com.ustadmobile.lib.db.entities.UmAccount;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_DASHBOARD_ENTRY_UID;
import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_OPTIONS;
import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_TYPE;

/**
 * Presenter for ReportDetailView view - common for every report view.
 **/
public class ReportDetailPresenter extends UstadBaseController<ReportDetailView> {


    UmAppDatabase repository;
    private ReportOptions reportOptions;
    private DashboardEntryDao entryDao;
    UstadMobileSystemImpl impl;
    long loggedInPersonUid;
    private String reportOptionsString;
    private SaleDao saleDao;
    long dashboardEntryUid;
    private String reportTitle;
    private int reportType = 0;



    public ReportDetailPresenter(Object context, Hashtable arguments, ReportDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        impl = UstadMobileSystemImpl.getInstance();
        entryDao = repository.getDashboardEntryDao();
        saleDao = repository.getSaleDao();

        UmAccount activeAccount = UmAccountManager.getActiveAccount(context);

        if(activeAccount != null) {
            loggedInPersonUid = activeAccount.getPersonUid();
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if (getArguments().containsKey(ARG_DASHBOARD_ENTRY_UID)) {
            dashboardEntryUid = Long.valueOf(
                    getArguments().get(ARG_DASHBOARD_ENTRY_UID).toString());
            entryDao.findByUidAsync(dashboardEntryUid, new UmCallback<DashboardEntry>() {
                @Override
                public void onSuccess(DashboardEntry result) {
                    view.showAddToDashboardButton(false);
                    view.setTitle(result.getDashboardEntryTitle());
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });

        }else{
            view.showAddToDashboardButton(true);
        }

        view.showDownloadButton(true);

        if (getArguments().containsKey(ARG_REPORT_TYPE)) {
            reportType = Integer.valueOf(getArguments().get(ARG_REPORT_TYPE).toString());
            view.setReportType(reportType);
        }
        if(getArguments().containsKey(ARG_REPORT_OPTIONS)){
            reportOptionsString = getArguments().get(ARG_REPORT_OPTIONS).toString();
        }

        switch(reportType){
            case DashboardEntry.REPORT_TYPE_SALES_PERFORMANCE:
                view.showSalesPerformanceReport();
                break;
            case DashboardEntry.REPORT_TYPE_SALES_LOG:
                view.showSalesLogReport();
                break;
            case DashboardEntry.REPORT_TYPE_TOP_LES:
                view.showTopLEsReport();
                break;
            default:
                break;
        }

    }

    public void handleClickAddToDashboard() {
        DashboardEntry newEntry = new DashboardEntry(reportTitle,
                reportType, loggedInPersonUid);
        newEntry.setDashboardEntryReportParam(reportOptionsString);
        entryDao.insertAsync(newEntry, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public void handleClickEditReport() {
        view.finish();
        Hashtable<String, String> args = new Hashtable<>();
        if(dashboardEntryUid != 0)
            args.put(ARG_DASHBOARD_ENTRY_UID, String.valueOf(dashboardEntryUid));
        if(reportOptionsString!= null && !reportOptionsString.isEmpty())
            args.put(ARG_REPORT_OPTIONS, reportOptionsString);
        args.put(ARG_REPORT_TYPE, String.valueOf(reportType));

        impl.go(ReportOptionsDetailView.VIEW_NAME, args, context);
    }

    public void handleClickDownloadReport() {
        //TODO:
    }
}
