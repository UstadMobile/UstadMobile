package com.ustadmobile.core.db.dao;

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


    @UmQuery("SELECT * FROM ScheduledCheck WHERE checkType = :checkType")
    public abstract List<ScheduledCheck> findByCheckTypeAndArgs(int checkType);

    @UmQuery("SELECT * FROM ScheduledCheck")
    public abstract List<ScheduledCheck> findAll();

    @UmDelete
    public abstract void deleteCheck(ScheduledCheck scheduledCheck);

    @UmQuery("SELECT ScheduledCheck.* FROM ScheduledCheck WHERE checkUuid IS NULL")
    public abstract List<ScheduledCheck> findAllChecksWhereCheckUuidIsNull();

    @UmQuery("UPDATE ScheduledCheck SET checkUuid = :checkUuid WHERE scheduledCheckId = :scheduledCheckId")
    public abstract void updateCheckUuid(long scheduledCheckId, String checkUuid);

    @UmQuery("SELECT ClazzLog.* FROM ClazzLog " +
            "LEFT JOIN ScheduledCheck ON ClazzLog.clazzLogUid = ScheduledCheck.scClazzLogUid AND " +
            "ScheduledCheck.checkType = :checkType " +
            "WHERE ScheduledCheck.scClazzLogUid IS NULL")
    public abstract List<ClazzLog> findPendingLogsWithoutScheduledCheck(int checkType);

    /**
     * ScheduledCheck runs locally, and is not sync'd. New ClazzLog objects can be created
     * by presenters, or the scheduler, but they might also arrive through the sync system (e.g.
     * for someone who just logged in).
     */
    public void createPendingScheduledChecks() {
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


        List<ClazzLog> logsWithoutNextDayCheck = findPendingLogsWithoutScheduledCheck(
                ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER);
        for(ClazzLog clazzLog : logsWithoutNextDayCheck) {
            long checkTime = clazzLog.getLogDate();
            //TODO: Advance to the next morning. Create a Calendar, add one day of ms
            ScheduledCheck nextDayCheck = new ScheduledCheck(
                    checkTime,
                    ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER,
                    clazzLog.getClazzLogUid());
        }
    }



}
