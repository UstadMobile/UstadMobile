package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;

import java.util.Calendar;
import java.util.Hashtable;

public class ClazzLogDetailPresenter extends UstadBaseController<ClassLogDetailView> {

    private long currentClazzLogUId = -1L;
    private long currentClazzUid = -1L;
    private long currentLogDate = -1L;

    private UmProvider<ClazzLogAttendanceRecord> clazzLogAttendanceRecordUmProvider;

    private ClazzLog clazzLog;

    /**
     * Constructor. We get the ClazzLog Uid from the arguments
     *
     * @param context
     * @param arguments
     * @param view
     */
    public ClazzLogDetailPresenter(Object context,
                                   Hashtable arguments,
                                   ClassLogDetailView view) {
        super(context, arguments, view);

        if(arguments.containsKey("clazzloguid")){
            currentClazzLogUId = (long) arguments.get("clazzloguid");
        }
        if(arguments.containsKey("clazzuid")){
            currentClazzUid = (long) arguments.get("clazzuid");
        }
        if(arguments.containsKey("logdate")){
            currentLogDate = (long) arguments.get("logdate");
        }
    }

    /**
     * UstadBaseController\s setUiString()
     * Doesn't do anything here.
     */
    @Override
    public void setUIStrings() {

    }

    /**
     * The Presenter's onCreate. This populated the provider and sets it to the View.
     *
     * This will be called when the implementation view is ready.
     * (ie: on Android, this is called in the Activity's onCreateView() )
     *
     * @param savedState This is generally the state which Android resumes this app. This is not
     *                   the arguments. It will most likely be null in a normal application run.
      */
    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);

        //Check for ClassLog
        ClazzLogDao clazzLogDao = UmAppDatabase.getInstance(getContext()).getClazzLogDao();
        ClazzLogAttendanceRecordDao clazzLogAttendanceRecordDao =
                UmAppDatabase.getInstance(getContext()).getClazzLogAttendanceRecordDao();

        Calendar attendanceDate = Calendar.getInstance();
        attendanceDate.setTimeInMillis(currentLogDate);
        attendanceDate.set(Calendar.HOUR_OF_DAY, 0);
        attendanceDate.set(Calendar.MINUTE, 0);
        attendanceDate.set(Calendar.SECOND, 0);
        attendanceDate.set(Calendar.MILLISECOND, 0);

        this.currentLogDate = attendanceDate.getTimeInMillis();

        clazzLogDao.findByClazzIdAndDateAsync(currentClazzUid, currentLogDate, new UmCallback<ClazzLog>() {
            @Override
            public void onSuccess(ClazzLog result) {
                clazzLog = result;
                clazzLogAttendanceRecordDao.insertAllAttendanceRecords(currentClazzUid,
                        result.getClazzLogUid(), new UmCallback<long[]>() {
                    @Override
                    public void onSuccess(long[] result2) {
                        //Get provider
                        clazzLogAttendanceRecordUmProvider = UmAppDatabase.getInstance(context)
                                .getClazzLogAttendanceRecordDao()
                                .findAttendanceLogsByClassLogId(result.getClazzLogUid());
                        //Set to view
                        view.runOnUiThread(() ->
                                view.setClazzLogAttendanceRecordProvider(clazzLogAttendanceRecordUmProvider));
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        System.out.println(exception);
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                System.out.println(exception);
            }
        });


    }

    /**
     * Method logic to what happens when we click "Done" on the ClassLogDetail View
     */
    public void handleClickDone(){
        //TODO: This
        System.out.println("handle Click Done");

    }

    /**
     * Method logic for what happens when we change the order of the student list.
     *
     * @param order The order flag. 0 to Sort by Name, 1 to Sort by Attendance, 2 to Sort by date.
     */
    public void handleChangeSortOrder(int order){
        //TODO: this
        System.out.println("Sort");
    }

    /**
     * Handle when the user taps to mark all present, or mark all absent. This will update the
     * database to set all ClazzLogAttendanceRecord
     *
     * @param attendanceStatus attendance status to set for all ClazzLogAttendanceRecords that are in this ClazzLog
     */
    public void handleMarkAll(int attendanceStatus){
        UmAppDatabase.getInstance(context).getClazzLogAttendanceRecordDao()
                .updateAllByClazzLogUid(clazzLog.getClazzLogUid(), attendanceStatus, null);
    }

    public void handleMarkStudent(long clazzLogAttendanceRecordUid, int attendanceStatus) {
        UmAppDatabase.getInstance(context).getClazzLogAttendanceRecordDao()
                .updateAttendanceStatus(clazzLogAttendanceRecordUid, attendanceStatus, null);
    }

}
