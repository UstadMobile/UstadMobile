package com.ustadmobile.core.controller;

import com.google.gson.Gson;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.DashboardEntryDao;
import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.ReportOptions;
import com.ustadmobile.core.view.ReportBarChartComponentView;
import com.ustadmobile.lib.db.entities.ReportSalesPerformance;
import com.ustadmobile.lib.db.entities.UmAccount;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_OPTIONS;


/**
 * Presenter for ReportBarChartComponentView view
 **/
public class ReportChartViewComponentPresenter extends UstadBaseController<ReportBarChartComponentView> {

    UmAppDatabase repository;
    ReportOptions reportOptions;
    DashboardEntryDao entryDao;
    UstadMobileSystemImpl impl;
    long loggedInPersonUid;
    String reportOptionsString;
    private SaleDao saleDao;

    public ReportChartViewComponentPresenter(Object context, Hashtable arguments,
                                             ReportBarChartComponentView view) {
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

        if(getArguments().containsKey(ARG_REPORT_OPTIONS)){
            reportOptionsString = getArguments().get(ARG_REPORT_OPTIONS).toString();
            Gson gson = new Gson();
            reportOptions = gson.fromJson(reportOptionsString, ReportOptions.class);

            int startOfWeek = 6; //Sunday //TODO: GET THIS FROM SETTINGS, etc/
            List<Long> producerUids= new ArrayList<>();

            saleDao.getSalesPerformanceReportSumGroupedByLocation(reportOptions.getLes(),
                producerUids, reportOptions.getLocations(), reportOptions.getProductTypes(),
                reportOptions.getFromDate(), reportOptions.getToDate(),
                reportOptions.getFromPrice(), reportOptions.getToPrice(),
                new UmCallback<List<ReportSalesPerformance>>() {
                    @Override
                    public void onSuccess(List<ReportSalesPerformance> result) {
                        view.setChartData((List<Object>)(List<?>)result);

                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
        }

    }


}
