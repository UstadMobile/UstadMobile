package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.ReportDetailView;
import com.ustadmobile.core.view.ReportTopLEsDetailView;


/**
 * Presenter for ReportTopLEsDetail view
 **/
public class ReportTopLEsDetailPresenter extends ReportDetailPresenter<ReportTopLEsDetailView> {

    UmAppDatabase repository;


    public ReportTopLEsDetailPresenter(Object context, Hashtable arguments, ReportTopLEsDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }


    @Override
    public void handleCommonPressed(Object arg, Object arg2) {

    }

    @Override
    public void handleSecondaryPressed(Object arg) {

    }

    @Override
    public void handleClickAddToDashboard() {

    }
}
