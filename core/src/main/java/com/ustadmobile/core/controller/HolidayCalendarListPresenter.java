package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import java.util.Hashtable;

import com.ustadmobile.core.view.HolidayCalendarListView;
import com.ustadmobile.core.view.HolidayCalendarDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Holiday;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.HolidayDao;

/**
 *  Presenter for HolidayCalendarList view
**/
public class HolidayCalendarListPresenter extends UstadBaseController<HolidayCalendarListView> {

        private UmProvider<Holiday> umProvider;
            UmAppDatabase repository;
    private HolidayDao providerDao;
        
    

    public HolidayCalendarListPresenter(Object context, Hashtable arguments, HolidayCalendarListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        
        //Get provider Dao
        providerDao = repository.getHolidayDao();
                
        
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

                //Get provider 
        umProvider = providerDao.findAllHolidays();
        view.setListProvider(umProvider);
                
    }

    public void handleClickPrimaryActionButton(){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(HolidayCalendarDetailView.VIEW_NAME, args, context);
    }
    
}
