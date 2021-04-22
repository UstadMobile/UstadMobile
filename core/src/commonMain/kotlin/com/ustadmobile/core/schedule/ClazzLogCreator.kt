package com.ustadmobile.core.schedule

import com.soywiz.klock.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.Holiday

/**
 * This method will generate ClazzLog entities for a given Clazz for each time a class is expected
 * to happen within a given timerange according to the related Schedule and HolidayCalendar entities.
 *
 * If the day the class is expected to take place is a holiday, then a ClazzLog will be created but
 * it will be marked as cancelled.
 *
 * This function should be called at (or just after) midnight local time (according to the class)
 * timezone.
 *
 * @param fromTime the start time of the range for which clazzlogs will be created (inclusive)
 * @param endTime the end time of the range for which clazzlogs will be created (inclusive)
 * @param clazzUid the clazzUid for the Clazz for which ClazzLogs will be created
 *
 * @return the next time that we need to check for clazz log creation. This will be midnight on the
 * day of the next occurrence of the class.
 */
fun UmAppDatabase.createClazzLogs(fromTime: Long, toTime: Long, clazzUid: Long) : Long{

    val holidayCalendarHolidayLists = mutableMapOf<Long, List<Holiday>>()
    val toDateTime = DateTime.fromUnix(toTime)

    val clazz = clazzDao.findClazzWithEffectiveHolidayCalendarAndFilter(clazzUid)
        ?: throw IllegalArgumentException("createClazzLogs: Clazz $clazzUid does not exist!!")

    val alreadyCreatedClazzLogs = clazzLogDao.findByClazzUidWithinTimeRange(clazz.clazzUid,
            fromTime, toTime)


    val effectiveTimeZone = clazz.clazzTimeZone ?: clazz.school?.schoolTimeZone ?: "UTC"
    val startTimeOffset = getTimezoneOffset(effectiveTimeZone, fromTime)
    val fromTimeLocal = DateTime.fromUnix(fromTime).toOffset(
            TimezoneOffset(startTimeOffset.toDouble()))
    val fromDayOfWeekLocal = fromTimeLocal.dayOfWeek

    val holCalendarUid = clazz.holidayCalendar?.umCalendarUid ?: 0L
    val clazzHolidayList = holidayCalendarHolidayLists.getOrPut(holCalendarUid) {
        holidayDao.findByHolidayCalendaUid(holCalendarUid)
    }

    val clazzSchedules = scheduleDao.findAllSchedulesByClazzUidAsList(clazz.clazzUid)
    for (schedule in clazzSchedules) {
        val scheduleNextInstance = schedule.nextOccurence(effectiveTimeZone, fromTime)
        if (scheduleNextInstance >= toDateTime) {
            continue
        }

        val holidayAndDateTimeRange = clazzHolidayList.map {
            val timezoneOffset = getTimezoneOffset(effectiveTimeZone, it.holStartTime)
            Pair(it, DateTime.fromUnix(
                    it.holStartTime - timezoneOffset) until
                    DateTime.fromUnix(it.holEndTime - timezoneOffset))
        }

        val overlappingHolidays = holidayAndDateTimeRange
                .filter { it.second.contains(scheduleNextInstance) }

        val clazzLogDate = scheduleNextInstance.from.unixMillisLong
        val clazzLog = ClazzLog().apply {
            logDate = clazzLogDate
            logDuration = scheduleNextInstance.duration.millisecondsInt
            clazzLogClazzUid = clazz.clazzUid
            clazzLogScheduleUid = schedule.scheduleUid
            clazzLogUid = generateUid()
            clazzLogCancelled = overlappingHolidays.isNotEmpty()
            if (clazzLogCancelled) {
                cancellationNote = overlappingHolidays.joinToString {
                    it.first.holName ?: ""
                }
            }
        }

        //Check to see if the schedule has been updated. If it has been updated, then mark the
        // old entry status as STATUS_RESCHEDULED and then update the join for any related
        // ClazzLogAttendanceRecord entries
        val logsToReschedule = alreadyCreatedClazzLogs.filter {
            it.clazzLogScheduleUid == schedule.scheduleUid && it.clazzLogUid != clazzLog.clazzLogUid
        }

        logsToReschedule.forEach {
            clazzLogDao.updateStatusByClazzLogUid(it.clazzLogUid, ClazzLog.STATUS_RESCHEDULED)
            clazzLogAttendanceRecordDao.updateRescheduledClazzLogUids(it.clazzLogUid,
                    clazzLog.clazzLogUid)
        }

        if (!alreadyCreatedClazzLogs.any { it.clazzLogUid == clazzLog.clazzLogUid }) {
            clazzLogDao.insert(clazzLog)
        }

    }

    return clazzSchedules.nextOccurence(effectiveTimeZone, after = toTime + 1)?.from
        ?.toLocalMidnight(effectiveTimeZone)?.unixMillisLong ?: 0L
}