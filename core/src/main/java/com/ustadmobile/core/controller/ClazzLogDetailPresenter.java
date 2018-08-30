package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson;

import java.util.Calendar;
import java.util.Hashtable;

import static com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.*;

public class ClazzLogDetailPresenter extends UstadBaseController<ClassLogDetailView> {

    private long currentClazzUid = -1L;
    private long currentLogDate = -1L;

    private UmProvider<ClazzLogAttendanceRecordWithPerson> clazzLogAttendanceRecordUmProvider;

    private ClazzLog currentClazzLog;

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

        if(arguments.containsKey("clazzuid")){
            currentClazzUid = Long.parseLong(arguments.get("clazzuid").toString());
        }
        if(arguments.containsKey("logdate")){
            String thisLogDate = arguments.get("logdate").toString();
            currentLogDate = Long.parseLong(thisLogDate);
        }
    }

    /**
     * UstadBaseController\s setUiString()
     * Doesn't do anything here.
     */
    @Override
    public void setUIStrings() {

    }

    public long getTodayMillis(){
        Calendar attendanceDate = Calendar.getInstance();
        attendanceDate.setTimeInMillis(System.currentTimeMillis());
        attendanceDate.set(Calendar.HOUR_OF_DAY, 0);
        attendanceDate.set(Calendar.MINUTE, 0);
        attendanceDate.set(Calendar.SECOND, 0);
        attendanceDate.set(Calendar.MILLISECOND, 0);
        return attendanceDate.getTimeInMillis();
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

                if(result == null){
                    //Create one anyway if not set for today
                    clazzLogDao.createClazzLogForDate(currentClazzUid, getTodayMillis(), new UmCallback<Long>() {
                        @Override
                        public void onSuccess(Long result) {

                            currentClazzLog = clazzLogDao.findByUid(result);
                            insertAllAndSetProvider(currentClazzLog);
                        }
                        @Override
                        public void onFailure(Throwable exception) {

                        }
                    });
                }else{
                    currentClazzLog = result;
                    insertAllAndSetProvider(currentClazzLog);
                }

            }

            @Override
            public void onFailure(Throwable exception) {
                System.out.println(exception);
            }
        });


    }

    /**
     * Common method to insert all attendance records for a clazz log uid and prepare its provider
     * to be set to the view.
     */
    public void insertAllAndSetProvider(ClazzLog result){

        ClazzLogAttendanceRecordDao clazzLogAttendanceRecordDao =
                UmAppDatabase.getInstance(getContext()).getClazzLogAttendanceRecordDao();

        clazzLogAttendanceRecordDao.insertAllAttendanceRecords(currentClazzUid,
                result.getClazzLogUid(), new UmCallback<long[]>() {
                    @Override
                    public void onSuccess(long[] result2) {
                        //Get provider
                        clazzLogAttendanceRecordUmProvider = UmAppDatabase.getInstance(context)
                                .getClazzLogAttendanceRecordDao()
                                .findAttendanceRecordsWithPersonByClassLogId(result.getClazzClazzUid());
                                //.findAttendanceLogsByClassLogId(result.getClazzLogUid());
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

    /**
     * Method logic to what happens when we click "Done" on the ClassLogDetail View
     */
    public void handleClickDone(){
        //1. Update Done status on ClazzLog for this clazzLogUid
        ClazzLogDao clazzLogDao = UmAppDatabase.getInstance(getContext()).getClazzLogDao();
        ClazzDao clazzDao = UmAppDatabase.getInstance(getContext()).getClazzDao();
        ClazzMemberDao clazzMemberDao = UmAppDatabase.getInstance(getContext()).getClazzMemberDao();
        ClazzLogAttendanceRecordDao clazzLogAttendanceRecordDao =
                UmAppDatabase.getInstance(getContext()).getClazzLogAttendanceRecordDao();
        clazzLogDao.updateDoneForClazzLogAsync(currentClazzLog.getClazzLogUid(),
                new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                //2. Update Attendance numbers for this clazzUid
                clazzDao.updateAttendancePercentage(currentClazzUid);
                //3. Update Attendance numbers for ClazzMember for this clazzUid.
                clazzMemberDao.updateAttendancePercentages(currentClazzUid);

                int numPresent = clazzLogAttendanceRecordDao.getAttedanceStatusCount(
                        currentClazzLog.getClazzLogUid(), STATUS_ATTENDED);
                int numAbsent = clazzLogAttendanceRecordDao.getAttedanceStatusCount(
                        currentClazzLog.getClazzLogUid(), STATUS_ABSENT);
                int numPartial = clazzLogAttendanceRecordDao.getAttedanceStatusCount(
                        currentClazzLog.getClazzLogUid(), STATUS_PARTIAL);

                clazzLogDao.updateClazzAttendanceNumbersAsync(currentClazzLog.getClazzLogUid(),
                        numPresent, numAbsent, numPartial, null);

                //4. Close the activity.
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }

    /**
     * Method logic for what happens when we change the order of the student list.
     *
     * @param order The order flag. 0 to Sort by Name, 1 to Sort by Attendance, 2 to Sort by date.
     */
    public void handleChangeSortOrder(int order){
        //TODO: this
    }

    /**
     * Handle when the user taps to mark all present, or mark all absent. This will update the
     * database to set all ClazzLogAttendanceRecord
     *
     * @param attendanceStatus attendance status to set for all ClazzLogAttendanceRecords that are in this ClazzLog
     */
    public void handleMarkAll(int attendanceStatus){
        UmAppDatabase.getInstance(context).getClazzLogAttendanceRecordDao()
                .updateAllByClazzLogUid(currentClazzLog.getClazzLogUid(), attendanceStatus, null);
    }

    public void handleMarkStudent(long clazzLogAttendanceRecordUid, int attendanceStatus) {
        UmAppDatabase.getInstance(context).getClazzLogAttendanceRecordDao()
                .updateAttendanceStatus(clazzLogAttendanceRecordUid, attendanceStatus, null);
    }

}
