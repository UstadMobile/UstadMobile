package com.ustadmobile.core.controller;

import com.google.gson.Gson;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.DashboardEntryDao;
import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.ReportOptions;
import com.ustadmobile.core.view.ReportOptionsDetailView;
import com.ustadmobile.core.view.ReportSalesLogDetailView;
import com.ustadmobile.lib.db.entities.DashboardEntry;
import com.ustadmobile.lib.db.entities.ReportSalesLog;
import com.ustadmobile.lib.db.entities.UmAccount;

import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_DASHBOARD_ENTRY_UID;
import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_OPTIONS;
import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_TYPE;
import static com.ustadmobile.lib.db.entities.DashboardEntry.REPORT_TYPE_SALES_LOG;


/**
 * Presenter for ReportSalesLogDetail view
 **/
public class ReportSalesLogDetailPresenter
        extends ReportDetailPresenter<ReportSalesLogDetailView> {

    UmAppDatabase repository;
    ReportOptions reportOptions;
    DashboardEntryDao entryDao;
    UstadMobileSystemImpl impl;
    long loggedInPersonUid;
    String reportOptionsString;
    long dashboardEntryUid;
    private SaleDao saleDao;


    public ReportSalesLogDetailPresenter(Object context, Hashtable arguments,
                                         ReportSalesLogDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        impl = UstadMobileSystemImpl.getInstance();
        entryDao = repository.getDashboardEntryDao();

        UmAccount activeAccount = UmAccountManager.getActiveAccount(context);

        if(activeAccount != null) {
            loggedInPersonUid = activeAccount.getPersonUid();
        }

        saleDao = repository.getSaleDao();

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
            view.setReportType(REPORT_TYPE_SALES_LOG);
        }

        view.showDownloadButton(true);

        if(getArguments().containsKey(ARG_REPORT_OPTIONS)){
            reportOptionsString = getArguments().get(ARG_REPORT_OPTIONS).toString();
            Gson gson = new Gson();
            reportOptions = gson.fromJson(reportOptionsString, ReportOptions.class);

            //TODO: Check:
            saleDao.getSaleLog(
                    new UmCallback<List<ReportSalesLog>>() {
                        @Override
                        public void onSuccess(List<ReportSalesLog> result) {
                            view.runOnUiThread(() -> view.setReportData((List<Object>)(List<?>)result));

                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });
        }


    }

    @Override
    public void handleClickAddToDashboard() {
        DashboardEntry newEntry = new DashboardEntry(
                impl.getString(MessageID.sales_log_report, context),
                REPORT_TYPE_SALES_LOG, loggedInPersonUid);
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

    @Override
    public void handleClickEditReport() {
        view.finish();
        Hashtable<String, String> args = new Hashtable<>();
        if(dashboardEntryUid != 0)
            args.put(ARG_DASHBOARD_ENTRY_UID, String.valueOf(dashboardEntryUid));
        if(reportOptionsString!= null && !reportOptionsString.isEmpty())
            args.put(ARG_REPORT_OPTIONS, reportOptionsString);
        args.put(ARG_REPORT_TYPE, String.valueOf(REPORT_TYPE_SALES_LOG));
        impl.go(ReportOptionsDetailView.VIEW_NAME, args, context);
    }

    @Override
    public void handleClickDownloadReport() {
        //TODO:
    }
}
