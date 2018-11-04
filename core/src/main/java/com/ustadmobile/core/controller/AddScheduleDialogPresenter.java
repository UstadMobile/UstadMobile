package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ScheduleDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.AddScheduleDialogView;
import com.ustadmobile.lib.db.entities.Schedule;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;


public class AddScheduleDialogPresenter  extends UstadBaseController<AddScheduleDialogView> {


    String[] schedulePresets;
    String[] dayPresets;

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


    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);
        if(currentSchedule == null) {
            currentSchedule = new Schedule();
        }

    }

    public void handleAddSchedule(){
        currentSchedule.setScheduleClazzUid(currentClazzUid);
        scheduleDao.insertAsync(currentSchedule, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                //sup
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    public void handleCancelSchedule(){
        //Do nothing.
        currentSchedule = null;
    }

    public void handleScheduleFromTimeSelected(long time){
        currentSchedule.setSceduleStartTime(time);
    }

    public void handleScheduleToTimeSelected(long time){
        currentSchedule.setScheduleEndTime(time);
    }


    public void handleScheduleSelected(int position, long id){
        //TODO
        System.out.println("Schedule selected: " + position + " id: " + id);
        currentSchedule.setScheduleFrequency(position + 1);

    }

    public void handleDaySelected(int position, long id){
        //TODO
        System.out.println("Day selected: " + position + " id: " + id);
        currentSchedule.setScheduleDay(position + 1);
    }

    @Override
    public void setUIStrings() {

    }
}
