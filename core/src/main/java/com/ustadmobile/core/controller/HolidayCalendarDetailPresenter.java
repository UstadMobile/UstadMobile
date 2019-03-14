package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.DateRangeDao;
import com.ustadmobile.core.db.dao.UMCalendarDao;
import com.ustadmobile.core.impl.UmAccountManager;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.HolidayCalendarDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.DateRange;
import com.ustadmobile.lib.db.entities.UMCalendar;

import com.ustadmobile.core.db.UmAppDatabase;

import static com.ustadmobile.core.view.HolidayCalendarDetailView.ARG_CALENDAR_UID;
import static com.ustadmobile.core.view.HolidayCalendarDetailView.ARG_DATERANGE_UID;

/**
 * Presenter for HolidayCalendarDetail view
 **/
public class HolidayCalendarDetailPresenter extends UstadBaseController<HolidayCalendarDetailView> {

    private UmProvider<DateRange> umProvider;
    UmAppDatabase repository;
    private DateRangeDao providerDao;
    private long currentCalendarUid = 0;
    private UMCalendar currentCalendar;
    private UMCalendar updatedCalendar;
    UMCalendarDao umCalendarDao;

    public HolidayCalendarDetailPresenter(Object context, Hashtable arguments, HolidayCalendarDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getDateRangeDao();
        umCalendarDao = repository.getUMCalendarDao();

        if(arguments.containsKey(HolidayCalendarDetailView.ARG_CALENDAR_UID)){
            currentCalendarUid = (long) arguments.get(ARG_CALENDAR_UID);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(currentCalendarUid == 0){
            currentCalendar = new UMCalendar();
            currentCalendar.setUmCalendarActive(false);
            umCalendarDao.insertAsync(currentCalendar, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    initFromCalendar(result);
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });
        }else{
            initFromCalendar(currentCalendarUid);
        }



    }

    public void updateRanges(){
        //Get provider
        umProvider = providerDao.findAllDatesInCalendar(currentCalendarUid);
        view.setListProvider(umProvider);
    }

    private void initFromCalendar(long calUid) {
        this.currentCalendarUid = calUid;
        //Handle Clazz info changed:
        //Get person live data and observe
        UmLiveData<UMCalendar> calendarLiveData = umCalendarDao.findByUidLive(currentCalendarUid);
        //Observe the live data
        calendarLiveData.observe(HolidayCalendarDetailPresenter.this,
                HolidayCalendarDetailPresenter.this::handleCalendarValueChanged);

        umCalendarDao.findByUidAsync(currentCalendarUid, new UmCallback<UMCalendar>() {
            @Override
            public void onSuccess(UMCalendar result) {
                updatedCalendar = result;
                view.updateCalendarOnView(result);

                updateRanges();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });


    }

    private void handleCalendarValueChanged(UMCalendar calendar){
        //set the og person value
        if(currentCalendar == null)
            currentCalendar = calendar;

        if(updatedCalendar == null || !updatedCalendar.equals(calendar)) {
            //update class edit views
            view.updateCalendarOnView(updatedCalendar);
            //Update the currently editing class object
            updatedCalendar = calendar;
        }
    }


    public void handleAddDateRange(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        //TODO:
        //impl.go(AddDateRangeDialogView.VIEW_NAME, args, context);
    }

    public void handleEditRange(long rangeUid){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_DATERANGE_UID, rangeUid);
        //TODO:
        //impl.go(AddDateRangeDialogView.VIEW_NAME, args, context);
    }

    public void handleDeleteRange(long rangeUid){
        repository.getDateRangeDao().findByUidAsync(rangeUid, new UmCallback<DateRange>() {
            @Override
            public void onSuccess(DateRange result) {
                if (result != null){
                    result.setDateRangeActive(false);

                }
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }


    public void handleClickDone() {

        currentCalendar.setUmCalendarActive(true);
        repository.getUMCalendarDao().updateAsync(currentCalendar, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }
}
