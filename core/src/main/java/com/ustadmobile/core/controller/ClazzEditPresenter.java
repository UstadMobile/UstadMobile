package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ScheduleDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AddScheduleDialogView;
import com.ustadmobile.core.view.ClazzEditView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.Schedule;
import com.ustadmobile.lib.db.entities.UMCalendar;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.view.ClazzEditView.ARG_SCHEDULE_UID;
import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;


/**
 * The ClazzEdit Presenter - responsible for The logic behind editing a Clazz.
 * Usually called when adding a new class or editing a current one from the Class list and Class
 * Detail screens.
 */
public class ClazzEditPresenter
        extends CommonHandlerPresenter<ClazzEditView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private Clazz mOriginalClazz;
    private Clazz mUpdatedClazz;

    private UmProvider<Schedule> clazzScheduleLiveData;
    private UmLiveData<List<UMCalendar>> holidaysLiveData;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);
    private ClazzDao clazzDao = repository.getClazzDao();

    public ClazzEditPresenter(Object context, Hashtable arguments, ClazzEditView view) {
        super(context, arguments, view);

        //Get Clazz Uid and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

    }

    /**
     * In Order:
     *      1. Gets the clazz object
     *      2. Observes changes in the Class object and attaches to handleClazzValueChanged()
     *      3. Gets the Schedule live data for this class.
     *      4. Updates the view with the schedule provider.
     *
     * @param savedState The saved state
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Handle Clazz info changed:
        //Get person live data and observe
        UmLiveData<Clazz> clazzLiveData = clazzDao.findByUidLive(currentClazzUid);
        //Observe the live data
        clazzLiveData.observe(ClazzEditPresenter.this,
                ClazzEditPresenter.this::handleClazzValueChanged);

        clazzDao.findByUidAsync(currentClazzUid, new UmCallback<Clazz>() {
            @Override
            public void onSuccess(Clazz result) {
                mUpdatedClazz = result;
                view.updateClazzEditView(result);
                holidaysLiveData = repository.getUMCalendarDao()
                        .findAllUMCalendarsAsLiveDataList();
                holidaysLiveData.observe(ClazzEditPresenter.this,
                        ClazzEditPresenter.this::handleAllHolidaysChanged);
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        //Set Schedule live data:
        clazzScheduleLiveData = repository.getScheduleDao()
                .findAllSchedulesByClazzUid(currentClazzUid);
        updateViewWithProvider();

    }

    /**
     * Common method to update the provider st on this Presenter to the view.
     */
    private void updateViewWithProvider(){
        view.setClazzScheduleProvider(clazzScheduleLiveData);
    }

    /**
     * Handles the change in holidays called (mostly called from UMCalendar live data observing.
     * Upon this method call (ie: when calendar updates, it will set the Holiday presets on
     * the view.
     *
     * @param umCalendar The list of UMCalendar holidays
     */
    private void handleAllHolidaysChanged(List<UMCalendar> umCalendar) {
        int selectedPosition = 0;

        ArrayList<String> holidayList = new ArrayList<>();
        for(UMCalendar ec : umCalendar){
            holidayList.add(ec.getUmCalendarName());
        }
        String[] holidayPreset = new String[holidayList.size()];
        holidayPreset = holidayList.toArray(holidayPreset);

        if(mOriginalClazz == null){
            mOriginalClazz = new Clazz();
        }
        if(mOriginalClazz.getClazzHolidayUMCalendarUid() > 0){
            selectedPosition = (int) mOriginalClazz.getClazzHolidayUMCalendarUid();
        }

        view.setHolidayPresets(holidayPreset, selectedPosition);
    }

    /**
     * Updates the class name of the currently editing class. Does NOT persist the data.
     *
     * @param newName The class name
     */
    public void updateName(String newName){
        mUpdatedClazz.setClazzName(newName);
    }

    /**
     * Updates the class description of the currently editing class. Does NOT persist the data.
     * @param newDesc The new class description.
     */
    public void updateDesc(String newDesc){
        mUpdatedClazz.setClazzDesc(newDesc);
    }

    /**
     * Updates the class holiday calendar set to the currently editing class. Does NOT persist the
     * data to the database.
     *
     * @param position The position of the Holiday Calendars from the Holiday drop down preset.
     */
    public void updateHoliday(long position){
        mUpdatedClazz.setClazzHolidayUMCalendarUid(position);
    }

    /**
     * Method that handles every Class change (usually from observing an entry from the DB) that
     * gets set on this Presenter's onCreate(). Its job here is to update the current working
     * class and the view as well.
     *
     * This will get called every time we stat editing a class and again if the Class gets updated
     * while we are editing it (ie: on the screen and this presenter). While rare, we are handling
     * it.
     *
     * @param clazz The Class object that got updated.
     */
    private void handleClazzValueChanged(Clazz clazz){
        //set the og person value
        if(mOriginalClazz == null)
            mOriginalClazz = clazz;

        if(mUpdatedClazz == null || !mUpdatedClazz.equals(clazz)) {
            //update class edit views
            view.updateClazzEditView(mUpdatedClazz);
            //Update the currently editing class object
            mUpdatedClazz = clazz;
        }
    }

    /**
     * Handles what happens when Add Schedule button clicked - to add a new schedule to the Class.
     * Over here, we open the AddScheduleDialogView Dialog for that Class.
     */
    public void handleClickAddSchedule(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        impl.go(AddScheduleDialogView.VIEW_NAME, args, getContext());
    }

    /**
     * Handles when the Class Edit screen's done/tick button is pressed. This intent denotes
     * confirmation of all changes done in the screen. Hence, the method will persist the updated
     * Class object, set it to active, and finish(close) the screen.
     *
     */
    public void handleClickDone() {
        mUpdatedClazz.setClazzActive(true);

        clazzDao.updateAsync(mUpdatedClazz, new UmCallback<Integer>(){
            @Override
            public void onSuccess(Integer result) {
                //Close the activity.
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }


    /**
     * Handles the primary button pressed on the recycler adapter on the Class Edit page. This is
     * triggered from the options menu of each schedule in the Class edit screen. The primary task
     * here is to edit this Schedule assigned to this Clazz.
     *
     * @param arg Any argument needed - Not used here.
     */
    @Override
    public void handleCommonPressed(Object arg) {
        // To edit the schedule assigned to clazz
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        args.put(ARG_SCHEDULE_UID, arg);
        impl.go(AddScheduleDialogView.VIEW_NAME, args, getContext());
    }

    /**
     * Handles the secondary button pressed on the recycler adapter on the Class Edit page. This is
     * triggered from the options menu of each schedule in the Class edit screen. The secondary task
     * here is to remove this Schedule assigned to this Clazz.
     *
     * @param arg Any argument needed - The Schedule to be deleted's UID
     */
    @Override
    public void handleSecondaryPressed(Object arg) {
        //To delete schedule assigned to clazz
        ScheduleDao scheduleDao = repository.getScheduleDao();
        scheduleDao.disableSchedule((Long) arg);

    }
}
