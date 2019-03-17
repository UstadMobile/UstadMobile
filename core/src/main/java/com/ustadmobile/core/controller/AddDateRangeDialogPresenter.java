package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.DateRangeDao;
import com.ustadmobile.core.db.dao.ScheduleDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AddDateRangeDialogView;
import com.ustadmobile.core.view.AddScheduleDialogView;
import com.ustadmobile.lib.db.entities.DateRange;
import com.ustadmobile.lib.db.entities.Schedule;

import java.util.Hashtable;

import static com.ustadmobile.core.view.AddDateRangeDialogView.DATERANGE_UID;
import static com.ustadmobile.core.view.AddScheduleDialogView.EVERY_DAY_SCHEDULE_POSITION;
import static com.ustadmobile.core.view.ClazzEditView.ARG_SCHEDULE_UID;
import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.HolidayCalendarDetailView.ARG_CALENDAR_UID;


public class AddDateRangeDialogPresenter extends UstadBaseController<AddDateRangeDialogView> {

    private DateRange currentDateRange;

    private DateRangeDao dateRangeDao;

    private UmAppDatabase appDatabaseRepo;

    private long currentDateRangeUid = 0;
    private long currentCalendarUid = 0;

    public AddDateRangeDialogPresenter(Object context, Hashtable arguments, AddDateRangeDialogView view) {
        super(context, arguments, view);

        appDatabaseRepo = UmAccountManager.getRepositoryForActiveAccount(context);
        dateRangeDao = appDatabaseRepo.getDateRangeDao();

        if(getArguments().containsKey(ARG_CALENDAR_UID)){
            currentCalendarUid = (long) getArguments().get(ARG_CALENDAR_UID);
        }


        if(getArguments().containsKey(DATERANGE_UID)){
            currentDateRangeUid = (long) getArguments().get(DATERANGE_UID);
        }

        if(currentDateRangeUid > 0){
            dateRangeDao.findByUidAsync(currentDateRangeUid, new UmCallback<DateRange>() {
                @Override
                public void onSuccess(DateRange result) {
                    currentDateRange = result;
                    view.updateFields(result);
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });
        }else{
            currentDateRange = new DateRange();
        }
    }


    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);
        if(currentDateRange == null) {
            currentDateRange = new DateRange();
        }

    }

    /**
     * Handles what happens when you click OK/Add in the DateRange dialog - Persists the date range
     * to that clazz.
     */
    public void handleAddDateRange(){

        currentDateRange.setDateRangeActive(true);
        currentDateRange.setDateRangeUMCalendarUid(currentCalendarUid);

        if(currentDateRange.getDateRangeUid() == 0) { //Not persisted. Insert it.
            dateRangeDao.insertAsync(currentDateRange, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {}

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }else { //Update it.
            dateRangeDao.updateAsync(currentDateRange, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {}

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }
    }

    /**
     * Cancels the schedule dialog
     */
    public void handleCancelDateRange(){
        //Do nothing.
        currentDateRange = null;
    }

    /**
     * Sets the picked "from" time from the dialog to the daterange object in the presenter. In ms
     * since the start of the day.
     *
     * @param time  The "from" time.
     */
    public void handleDateRangeFromTimeSelected(long time){
        currentDateRange.setDateRangeFromDate(time);
    }

    /**
     * Sets the picked "to" time from the dialog to the daterange object in the presenter.
     *
     * @param time The "to" time
     */
    public void handleDateRangeToTimeSelected(long time){
        currentDateRange.setDateRangeToDate(time);
    }

}
