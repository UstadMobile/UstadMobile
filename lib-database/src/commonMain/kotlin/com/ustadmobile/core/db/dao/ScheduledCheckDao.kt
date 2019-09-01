package com.ustadmobile.core.db.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ScheduledCheck


@UmDao
@Dao
@UmRepository
abstract class ScheduledCheckDao : BaseDao<ScheduledCheck> {

    @Query("SELECT * FROM ScheduledCheck")
    abstract fun findAll(): List<ScheduledCheck>

    @Delete
    abstract fun deleteCheck(scheduledCheck: ScheduledCheck)

    @Query("SELECT ScheduledCheck.* FROM ScheduledCheck WHERE checkUuid IS NULL")
    abstract fun findAllChecksWhereCheckUuidIsNull(): List<ScheduledCheck>

    @Query("UPDATE ScheduledCheck SET checkUuid = :checkUuid " + " WHERE scheduledCheckUid = :scheduledCheckUid")
    abstract fun updateCheckUuid(scheduledCheckUid: Long, checkUuid: String)

    @Query("SELECT ClazzLog.* FROM ClazzLog " +
            " WHERE NOT EXISTS(SELECT scClazzLogUid FROM ScheduledCheck WHERE " +
            " scClazzLogUid = ClazzLog.clazzLogUid AND ScheduledCheck.checkType = :checkType) " +
            " AND ClazzLog.logDate >= :fromDate")
    abstract fun findPendingLogsWithoutScheduledCheck(checkType: Int, fromDate: Long): List<ClazzLog>

    @Query("SELECT * FROM ScheduledCheck WHERE scheduledCheckUid = :uid")
    abstract fun findByUid(uid:Long):ScheduledCheck?

//    fun findPendingLogsWithoutScheduledCheck(checkType: Int): List<ClazzLog> {
//
//        //TODO: KMP Fix
////        val todayCal = Calendar.getInstance()
////        todayCal.set(Calendar.HOUR_OF_DAY, 0)
////        todayCal.set(Calendar.MINUTE, 0)
////        todayCal.set(Calendar.SECOND, 0)
////        todayCal.set(Calendar.MILLISECOND, 0)
////        val todayZero = todayCal.getTimeInMillis()
////        return findPendingLogsWithoutScheduledCheck(checkType, todayZero)
//        return ArrayList<ClazzLog>()
//    }

    @Query("SELECT * From ClazzLog")
    abstract fun findAllClazzLogs(): List<ClazzLog>

    /**
     * ScheduledCheck runs locally, and is not sync'd. New ClazzLog objects can be created
     * by presenters, or the scheduler, but they might also arrive through the sync system (e.g.
     * for someone who just logged in).
     */
//    fun createPendingScheduledChecks() {
//
//        //TODO: KMP Fix
////        // The checks are created for clazz logs (if not already created by this method earlier)
////
////        //Get a list of all ClazzLogs that don't have a Schedule check of attendance reminder type.
////        //TYPE_RECORD_ATTENDANCE_REMINDER
////        val logsWithoutChecks = findPendingLogsWithoutScheduledCheck(
////                ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER)
////        val newCheckList = ArrayList<ScheduledCheck>()
////
////        for (clazzLog in logsWithoutChecks) {
////            val recordReminderCheck = ScheduledCheck(
////                    clazzLog.logDate,
////                    ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER,
////                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
////                            clazzLog.clazzLogUid)
////            recordReminderCheck.scClazzLogUid = clazzLog.clazzLogUid
////            newCheckList.add(recordReminderCheck)
////        }
////
////
////        //Create Scheduled Checks for repetition checks for TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER
////        val logsWithoutRepetitionChecks = findPendingLogsWithoutScheduledCheck(
////                ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER)
////
////        for (clazzLog in logsWithoutRepetitionChecks) {
////            val repetitionReminderOfficerCheck = ScheduledCheck(
////                    clazzLog.logDate,
////                    ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER,
////                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
////                            clazzLog.clazzLogClazzUid
////            )
////            repetitionReminderOfficerCheck.scClazzLogUid = clazzLog.clazzLogUid
////            newCheckList.add(repetitionReminderOfficerCheck)
////        }
////
////        //Create Scheduled Checks for TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH
////        val logsWithoutAbsentHighChecks = findPendingLogsWithoutScheduledCheck(
////                ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH)
////
////        for (clazzLog in logsWithoutAbsentHighChecks) {
////            val absentHighCheck = ScheduledCheck(
////                    clazzLog.logDate,
////                    ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER,
////                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
////                            clazzLog.clazzLogClazzUid
////            )
////            absentHighCheck.scClazzLogUid = clazzLog.clazzLogUid
////            newCheckList.add(absentHighCheck)
////        }
////
////        //Create Scheduled Checks for TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH
////        val logsWithoutClazzAttendanceHigh = findPendingLogsWithoutScheduledCheck(
////                ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH)
////        for (clazzLog in logsWithoutClazzAttendanceHigh) {
////            val clazzAttendanceHighSC = ScheduledCheck(
////                    clazzLog.logDate,
////                    ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH,
////                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
////                            clazzLog.clazzLogClazzUid
////            )
////            clazzAttendanceHighSC.scClazzLogUid = clazzLog.clazzLogUid
////            newCheckList.add(clazzAttendanceHighSC)
////        }
////
////        //Next day attendance taken or not checks. - Teachers, Officers, Admins get this
////        val logsWithoutNextDayCheck = findPendingLogsWithoutScheduledCheck(
////                ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER)
////
////        for (clazzLog in logsWithoutNextDayCheck) {
////
////            val clazzLogDateMilli = clazzLog.logDate
////            val clazzLogUid = clazzLog.clazzLogUid
////
////
////            //OR: We just Add one day from Clazzlog logdate
////            val clazzLogDate = Date(clazzLogDateMilli)
////            if (clazzLogDate != null) {
////                println("valid clazzlog")
////            }
////            //No need for timezone . It will be device midnight time.
////            val tomorrowSameTime = UMCalendarUtil.getDateInMilliPlusDaysRelativeTo(clazzLogDateMilli, 1)
////            val tomorrow = Calendar.getInstance()
////            val tomorrowSameTimeDate = Date(tomorrowSameTime)
////            tomorrow.setTime(tomorrowSameTimeDate)
////            tomorrow.set(Calendar.HOUR_OF_DAY, 0)
////            tomorrow.set(Calendar.MINUTE, 0)
////            tomorrow.set(Calendar.SECOND, 0)
////            tomorrow.set(Calendar.MILLISECOND, 0)
////
////            val tomorrowZeroHour = tomorrow.getTimeInMillis()
////            val tomorrowZeroDate = Date(tomorrowZeroHour)
////
////            if (tomorrowZeroDate != null) {
////                println("tomorrowZeroDate is null")
////            }
////
////            val nextDayCheck = ScheduledCheck(
////                    tomorrowZeroHour,
////                    ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER,
////                    ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
////                            clazzLogUid)
////            nextDayCheck.scClazzLogUid = clazzLogUid
////
////            newCheckList.add(nextDayCheck)
////
////        }
////
////        //Insert All new Scheduled Checks
////        insertList(newCheckList)
////
//    }

}
