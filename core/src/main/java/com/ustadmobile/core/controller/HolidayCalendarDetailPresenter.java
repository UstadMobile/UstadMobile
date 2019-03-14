package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.HolidayCalendarDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.UMCalendar;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.UMCalendarDao;

/**
 * Presenter for HolidayCalendarDetail view
 **/
public class HolidayCalendarDetailPresenter extends UstadBaseController<HolidayCalendarDetailView> {

    private UmProvider<UMCalendar> umProvider;
    UmAppDatabase repository;
    private UMCalendarDao providerDao;


    public HolidayCalendarDetailPresenter(Object context, Hashtable arguments, HolidayCalendarDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getUMCalendarDao();


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider 
        umProvider = providerDao.findAllUMCalendars();
        view.setListProvider(umProvider);

    }


    public void handleClickDone() {

        view.finish();
    }
}
