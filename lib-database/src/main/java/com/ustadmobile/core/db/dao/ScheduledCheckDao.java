package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDelete;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ScheduledCheck;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

import java.util.ArrayList;
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
            " scClazzLogUid = ClazzLog.clazzLogUid AND ScheduledCheck.checkType = :checkType)")
    public abstract List<ClazzLog> findPendingLogsWithoutScheduledCheck(int checkType);

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
            newCheckList.add(recordReminderCheck);
        }
        insertList(newCheckList);


        //Create Scheduled Checks for repetition checks for TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER
        List<ClazzLog> logsWithoutRepetitionChecks = findPendingLogsWithoutScheduledCheck(
                ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER);
        List<ScheduledCheck> repetitionOfficerChecks = new ArrayList<>();

        for(ClazzLog clazzLog: logsWithoutRepetitionChecks){
            ScheduledCheck repetitionReminderOfficerCheck = new ScheduledCheck(
                    clazzLog.getLogDate(),
                    ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER,
                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                            clazzLog.getClazzLogClazzUid()
            );
            repetitionOfficerChecks.add(repetitionReminderOfficerCheck);
        }
        insertList(repetitionOfficerChecks);

        //Create Scheduled Checks for TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH
        List<ClazzLog> logsWithoutAbsentHighChecks = findPendingLogsWithoutScheduledCheck(
                ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH);
        List<ScheduledCheck> absentHighChecks = new ArrayList<>();

        for(ClazzLog clazzLog: logsWithoutAbsentHighChecks){
            ScheduledCheck absentHighCheck = new ScheduledCheck(
                    clazzLog.getLogDate(),
                    ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER,
                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                            clazzLog.getClazzLogClazzUid()
            );
            absentHighChecks.add(absentHighCheck);
        }
        insertList(absentHighChecks);

        //Create Scheduled Checks for TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH
        List<ClazzLog> logsWithoutClazzAttendanceHigh = findPendingLogsWithoutScheduledCheck(
                ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH);
        List<ScheduledCheck> clazzAttendanceHighSCs = new ArrayList<>();
        for(ClazzLog clazzLog:logsWithoutClazzAttendanceHigh){
            ScheduledCheck clazzAttendanceHighSC = new ScheduledCheck(
                    clazzLog.getLogDate(),
                    ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH,
                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                            clazzLog.getClazzLogClazzUid()
            );
            clazzAttendanceHighSCs.add(clazzAttendanceHighSC);
        }
        insertList(clazzAttendanceHighSCs);


        //Next day attendance taken or not checks. - Teachers get
        List<ClazzLog> logsWithoutNextDayCheck = findPendingLogsWithoutScheduledCheck(
                ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER);
        List<ScheduledCheck> addThese = new ArrayList<>();
        for(ClazzLog clazzLog : logsWithoutNextDayCheck) {
            long checkTime = UMCalendarUtil.getDateInMilliPlusDaysRelativeTo(clazzLog.getLogDate(), 1);
            ScheduledCheck nextDayCheck = new ScheduledCheck(
                    checkTime,
                    ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER,
                    clazzLog.getClazzLogUid());

            addThese.add(nextDayCheck);
        }
        insertList(addThese);
    }



}
