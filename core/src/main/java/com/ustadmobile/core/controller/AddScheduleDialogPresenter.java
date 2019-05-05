package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ScheduleDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AddScheduleDialogView;
import com.ustadmobile.lib.db.entities.Schedule;

import java.util.Hashtable;

import static com.ustadmobile.core.view.AddScheduleDialogView.EVERY_DAY_SCHEDULE_POSITION;
import static com.ustadmobile.core.view.ClazzEditView.ARG_SCHEDULE_UID;
import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;


public class AddScheduleDialogPresenter  extends UstadBaseController<AddScheduleDialogView> {

    private Schedule currentSchedule;

    private ScheduleDao scheduleDao;

    private UmAppDatabase appDatabaseRepo;

    long currentClazzUid = -1;
    private long currentScheduleUid = -1L;

    /**
     * Initialises all Daos, gets all needed arguments and creates a schedule if argument not given.
     * Updates the schedule to the view.
     * @param context       Context of application
     * @param arguments     Arguments
     * @param view          View
     */
    public AddScheduleDialogPresenter(Object context, Hashtable arguments, AddScheduleDialogView view) {
        super(context, arguments, view);

        appDatabaseRepo = UmAccountManager.getRepositoryForActiveAccount(context);
        scheduleDao = appDatabaseRepo.getScheduleDao();

        if(getArguments().containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) getArguments().get(ARG_CLAZZ_UID);
        }

        if(getArguments().containsKey(ARG_SCHEDULE_UID)){
            currentScheduleUid = (long) getArguments().get(ARG_SCHEDULE_UID);
        }

        if(currentScheduleUid > 0){
            scheduleDao.findByUidAsync(currentScheduleUid, new UmCallback<Schedule>() {
                @Override
                public void onSuccess(Schedule result) {
                    currentSchedule = result;
                    view.updateFields(result);
                }

                @Override
                public void onFailure(Throwable exception) {exception.printStackTrace();}
            });
        }else{
            currentSchedule = new Schedule();
        }
    }


    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);
        if(currentSchedule == null) {
            currentSchedule = new Schedule();
        }
    }

    /**
     * Handles what happens when you click OK/Add in the Schedule dialog - Persists the schedule
     * to that clazz.
     */
    public void handleAddSchedule(){
        currentSchedule.setScheduleClazzUid(currentClazzUid);
        currentSchedule.setScheduleActive(true);

        //Creates ClazzLogs for today (since ClazzLogs are automatically only created for tomorrow)
        Runnable runAfterInsertOrUpdate = () -> {
            scheduleDao.createClazzLogsForToday(
                    UmAccountManager.getActivePersonUid(getContext()), appDatabaseRepo);
            //If you want it to create ClazzLogs for every day of schedule (useful for testing):
            //scheduleDao.createClazzLogsForEveryDayFromDays(5,
            //        UmAccountManager.getActivePersonUid(getContext()), appDatabaseRepo);

            UstadMobileSystemImpl.getInstance().scheduleChecks(getContext());
        };

        if(currentSchedule.getScheduleUid() == 0) {
            scheduleDao.insertAsync(currentSchedule, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    runAfterInsertOrUpdate.run();
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }else {
            scheduleDao.updateAsync(currentSchedule, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    appDatabaseRepo.getClazzLogDao().cancelFutureInstances(
                            currentScheduleUid, System.currentTimeMillis(), true);
                    runAfterInsertOrUpdate.run();
                }

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
    public void handleCancelSchedule(){
        currentSchedule = null;
    }

    /**
     * Sets the picked "from" time from the dialog to the schedule object in the presenter. In ms
     * since the start of the day.
     *
     * @param time  The "from" time.
     */
    public void handleScheduleFromTimeSelected(long time){
        currentSchedule.setSceduleStartTime(time);
    }

    /**
     * Sets the picked "to" time from the dialog to the schedule object in the presenter.
     *
     * @param time The "to" time
     */
    public void handleScheduleToTimeSelected(long time){
        currentSchedule.setScheduleEndTime(time);
    }

    /**
     * Sets schedule from the position of drop down options
     * @param position  Position of drop down (spinner) selected
     * @param id        If of drop down (spinner) selected
     */
    public void handleScheduleSelected(int position, long id){
        if(id == EVERY_DAY_SCHEDULE_POSITION){
            currentSchedule.setScheduleDay(-1);
            view.hideDayPicker(true);
        }else{
            view.hideDayPicker(false);
        }
        currentSchedule.setScheduleFrequency(position + 1);

    }

    /**
     * Sets schedule Day on the currently editing schedule.
     * @param position  The position of the day according to the drop down options.
     */
    public void handleDaySelected(int position){
        currentSchedule.setScheduleDay(position + 1);
    }

}
