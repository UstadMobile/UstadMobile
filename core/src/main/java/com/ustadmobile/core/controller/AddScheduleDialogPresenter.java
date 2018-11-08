package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ScheduleDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.AddScheduleDialogView;
import com.ustadmobile.lib.db.entities.Schedule;

import java.util.Hashtable;

import static com.ustadmobile.core.view.AddScheduleDialogView.EVERY_DAY_SCHEDULE_POSITION;
import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;


public class AddScheduleDialogPresenter  extends UstadBaseController<AddScheduleDialogView> {

    Schedule currentSchedule;
    ScheduleDao scheduleDao;
    long currentClazzUid = -1;

    public AddScheduleDialogPresenter(Object context, Hashtable arguments, AddScheduleDialogView view) {
        super(context, arguments, view);

        scheduleDao = UmAppDatabase.getInstance(context).getScheduleDao();
        currentSchedule = new Schedule();

        if(getArguments().containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) getArguments().get(ARG_CLAZZ_UID);
        }
    }


    /**
     * In Order:
     *          1. Just creates a new Schedule to be worked/edited in.
     *
     * @param savedState
     */
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
        scheduleDao.insertAsync(currentSchedule, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                //sup
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Cancels the schedule dialog
     */
    public void handleCancelSchedule(){
        //Do nothing.
        currentSchedule = null;
    }

    /**
     * Sets the picked "from" time from the dialog to the schedule object in the presenter.
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


    public void handleScheduleSelected(int position, long id){
        if(id == EVERY_DAY_SCHEDULE_POSITION){
            currentSchedule.setScheduleDay(-1);
            view.hideDayPicker(true);
        }else{
            view.hideDayPicker(false);
        }
        currentSchedule.setScheduleFrequency(position + 1);

    }

    public void handleDaySelected(int position, long id){
        currentSchedule.setScheduleDay(position + 1);
    }

    /**
     * Overridden method. Does nothing.
     */
    @Override
    public void setUIStrings() {

    }
}
