package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.DateRangeDao;
import com.ustadmobile.core.db.dao.UMCalendarDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.HolidayCalendarDetailView;
import com.ustadmobile.core.view.HolidayCalendarListView;
import com.ustadmobile.lib.db.entities.UMCalendar;
import com.ustadmobile.lib.db.entities.UMCalendarWithNumEntries;

import java.util.Hashtable;

import static com.ustadmobile.core.view.HolidayCalendarDetailView.ARG_CALENDAR_UID;

/**
 *  Presenter for HolidayCalendarList view
**/
public class HolidayCalendarListPresenter extends UstadBaseController<HolidayCalendarListView> {

        private UmProvider<UMCalendarWithNumEntries> umProvider;
            UmAppDatabase repository;
    private UMCalendarDao providerDao;
        
    

    public HolidayCalendarListPresenter(Object context, Hashtable arguments, HolidayCalendarListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        
        //Get provider Dao
        providerDao = repository.getUMCalendarDao();

        
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider
        umProvider = providerDao.findAllHolidaysWithEntriesCount();
        view.setListProvider(umProvider);
                
    }

    public void handleClickPrimaryActionButton(){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(HolidayCalendarDetailView.VIEW_NAME, args, context);
    }

    public void handleEditCalendar(long calendarUid){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_CALENDAR_UID, calendarUid);
        impl.go(HolidayCalendarDetailView.VIEW_NAME, args, context);
    }

    public void handleDeleteCalendar(long calendarUid){
        repository.getDateRangeDao().inactivateRange(calendarUid);
    }
    
}
