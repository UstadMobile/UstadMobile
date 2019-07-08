package com.ustadmobile.core.controller;

import com.google.gson.Gson;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.DashboardEntryDao;
import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.ReportOptions;
import com.ustadmobile.core.view.ReportTableListComponentView;
import com.ustadmobile.lib.db.entities.ReportTopLEs;
import com.ustadmobile.lib.db.entities.UmAccount;

import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_OPTIONS;

public class ReportTopLEsComponentPresenter extends UstadBaseController<ReportTableListComponentView> {

    UmAppDatabase repository;
    ReportOptions reportOptions;
    DashboardEntryDao entryDao;
    UstadMobileSystemImpl impl;
    long loggedInPersonUid;
    String reportOptionsString;
    private SaleDao saleDao;


    public ReportTopLEsComponentPresenter(Object context, Hashtable arguments,
                                          ReportTableListComponentView view) {
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

            saleDao.getTopLEs(new UmCallback<List<ReportTopLEs>>() {
                @Override
                public void onSuccess(List<ReportTopLEs> result) {
                    view.runOnUiThread(() -> view.setTopLEsData((List<Object>)(List<?>)result));

                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }


    }

}
