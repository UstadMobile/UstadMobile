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
import com.ustadmobile.lib.db.entities.ReportTopLEs;
import com.ustadmobile.core.view.ReportOptionsDetailView;
import com.ustadmobile.core.view.ReportTopLEsDetailView;
import com.ustadmobile.lib.db.entities.DashboardEntry;
import com.ustadmobile.lib.db.entities.UmAccount;

import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_DASHBOARD_ENTRY_UID;
import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_OPTIONS;
import static com.ustadmobile.lib.db.entities.DashboardEntry.REPORT_TYPE_TOP_LES;


/**
 * Presenter for ReportTopLEsDetail view
 **/
public class ReportTopLEsDetailPresenter extends ReportDetailPresenter<ReportTopLEsDetailView> {

    UmAppDatabase repository;
    ReportOptions reportOptions;
    DashboardEntryDao entryDao;
    UstadMobileSystemImpl impl;
    long loggedInPersonUid;
    String reportOptionsString;
    long dashboardEntryUid;

    private SaleDao saleDao;


    public ReportTopLEsDetailPresenter(Object context, Hashtable arguments, ReportTopLEsDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

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
            view.setReportType(REPORT_TYPE_TOP_LES);
        }

        view.showDownloadButton(true);

        if(getArguments().containsKey(ARG_REPORT_OPTIONS)){
            reportOptionsString = getArguments().get(ARG_REPORT_OPTIONS).toString();
            Gson gson = new Gson();
            reportOptions = gson.fromJson(reportOptionsString, ReportOptions.class);

            //TODO: Check:

            saleDao.getTopLEs(new UmCallback<List<ReportTopLEs>>() {
                        @Override
                        public void onSuccess(List<ReportTopLEs> result) {
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
                impl.getString(MessageID.sales_performance_report, context),
                REPORT_TYPE_TOP_LES, loggedInPersonUid);
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
        args.put(ARG_REPORT_OPTIONS, reportOptionsString);
        if(dashboardEntryUid != 0)
            args.put(ARG_DASHBOARD_ENTRY_UID, String.valueOf(dashboardEntryUid));
        impl.go(ReportOptionsDetailView.VIEW_NAME, args, context);
    }

    @Override
    public void handleClickDownloadReport() {
        //TODO
    }
}
