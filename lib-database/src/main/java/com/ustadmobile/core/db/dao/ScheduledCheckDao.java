package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDelete;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ScheduledCheck;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@UmDao
public abstract class ScheduledCheckDao implements BaseDao<ScheduledCheck> {

    @UmQuery("SELECT * FROM ScheduledCheck")
    public abstract List<ScheduledCheck> findAll();

    @UmDelete
    public abstract void deleteCheck(ScheduledCheck scheduledCheck);

    @UmQuery("SELECT ScheduledCheck.* FROM ScheduledCheck WHERE checkUuid IS NULL")
    public abstract List<ScheduledCheck> findAllChecksWhereCheckUuidIsNull();

    @UmQuery("UPDATE ScheduledCheck SET checkUuid = :checkUuid " +
            " WHERE scheduledCheckId = :scheduledCheckId")
    public abstract void updateCheckUuid(long scheduledCheckId, String checkUuid);

    @UmQuery("SELECT ClazzLog.* FROM ClazzLog " +
            " WHERE NOT EXISTS(SELECT scClazzLogUid FROM ScheduledCheck WHERE " +
            " scClazzLogUid = ClazzLog.clazzLogUid AND ScheduledCheck.checkType = :checkType) " +
            " AND ClazzLog.logDate >= :fromDate")
    public abstract List<ClazzLog> findPendingLogsWithoutScheduledCheck(int checkType, long fromDate);

    public List<ClazzLog> findPendingLogsWithoutScheduledCheck(int checkType){
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        long todayZero = todayCal.getTimeInMillis();
        return findPendingLogsWithoutScheduledCheck(checkType, todayZero);
    }

    @UmQuery("SELECT * From ClazzLog")
    public abstract List<ClazzLog> findAllClazzLogs();
    /**
     * ScheduledCheck runs locally, and is not sync'd. New ClazzLog objects can be created
     * by presenters, or the scheduler, but they might also arrive through the sync system (e.g.
     * for someone who just logged in).
     */
    public void createPendingScheduledChecks() {

        // The checks are created for clazz logs (if not already created by this method earlier)

        //Get a list of all ClazzLogs that don't have a Schedule check of attendance reminder type.
        //TYPE_RECORD_ATTENDANCE_REMINDER
        List<ClazzLog> logsWithoutChecks = findPendingLogsWithoutScheduledCheck(
                ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER);
        List<ScheduledCheck> newCheckList = new ArrayList<>();

        for(ClazzLog clazzLog : logsWithoutChecks) {
            ScheduledCheck recordReminderCheck = new ScheduledCheck(
                    clazzLog.getLogDate(),
                    ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER,
                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                            clazzLog.getClazzLogUid());
            recordReminderCheck.setScClazzLogUid(clazzLog.getClazzLogUid());
            newCheckList.add(recordReminderCheck);
        }


        //Create Scheduled Checks for repetition checks for TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER
        List<ClazzLog> logsWithoutRepetitionChecks = findPendingLogsWithoutScheduledCheck(
                ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER);

        for(ClazzLog clazzLog: logsWithoutRepetitionChecks){
            ScheduledCheck repetitionReminderOfficerCheck = new ScheduledCheck(
                    clazzLog.getLogDate(),
                    ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER,
                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                            clazzLog.getClazzLogClazzUid()
            );
            repetitionReminderOfficerCheck.setScClazzLogUid(clazzLog.getClazzLogUid());
            newCheckList.add(repetitionReminderOfficerCheck);
        }

        //Create Scheduled Checks for TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH
        List<ClazzLog> logsWithoutAbsentHighChecks = findPendingLogsWithoutScheduledCheck(
                ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH);

        for(ClazzLog clazzLog: logsWithoutAbsentHighChecks){
            ScheduledCheck absentHighCheck = new ScheduledCheck(
                    clazzLog.getLogDate(),
                    ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER,
                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                            clazzLog.getClazzLogClazzUid()
            );
            absentHighCheck.setScClazzLogUid(clazzLog.getClazzLogUid());
            newCheckList.add(absentHighCheck);
        }

        //Create Scheduled Checks for TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH
        List<ClazzLog> logsWithoutClazzAttendanceHigh = findPendingLogsWithoutScheduledCheck(
                ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH);
        for(ClazzLog clazzLog:logsWithoutClazzAttendanceHigh){
            ScheduledCheck clazzAttendanceHighSC = new ScheduledCheck(
                    clazzLog.getLogDate(),
                    ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH,
                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                            clazzLog.getClazzLogClazzUid()
            );
            clazzAttendanceHighSC.setScClazzLogUid(clazzLog.getClazzLogUid());
            newCheckList.add(clazzAttendanceHighSC);
        }

        //Next day attendance taken or not checks. - Teachers, Officers, Admins get this
        List<ClazzLog> logsWithoutNextDayCheck = findPendingLogsWithoutScheduledCheck(
                ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER);

        for(ClazzLog clazzLog : logsWithoutNextDayCheck) {

            long clazzLogDateMilli = clazzLog.getLogDate();
            long clazzLogUid = clazzLog.getClazzLogUid();


            //OR: We just Add one day from Clazzlog logdate
            Date clazzLogDate = new Date(clazzLogDateMilli);
            if(clazzLogDate != null){
                System.out.println("valid clazzlog");
            }
            //No need for timezone . It will be device midnight time.
            long tomorrowSameTime =
                    UMCalendarUtil.getDateInMilliPlusDaysRelativeTo(clazzLogDateMilli, 1);
            Calendar tomorrow = Calendar.getInstance();
            Date tomorrowSameTimeDate = new Date(tomorrowSameTime);
            tomorrow.setTime(tomorrowSameTimeDate);
            tomorrow.set(Calendar.HOUR_OF_DAY, 0);
            tomorrow.set(Calendar.MINUTE, 0);
            tomorrow.set(Calendar.SECOND, 0);
            tomorrow.set(Calendar.MILLISECOND, 0);

            long tomorrowZeroHour = tomorrow.getTimeInMillis();
            Date tomorrowZeroDate = new Date(tomorrowZeroHour);

            if(tomorrowZeroDate != null){
                System.out.println("tomorrowZeroDate is null");
            }

            ScheduledCheck nextDayCheck = new ScheduledCheck(
                    tomorrowZeroHour,
                    ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER,
                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                            clazzLogUid);
            nextDayCheck.setScClazzLogUid(clazzLogUid);

            newCheckList.add(nextDayCheck);

        }

        //Insert All new Scheduled Checks
        insertList(newCheckList);

    }



}
