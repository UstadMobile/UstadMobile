package com.ustadmobile.core.schedule

import com.soywiz.klock.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.Holiday
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * This method will create the ClazzLog records for each day. A ClazzLog is created for each day
 * that a class would be expected to take place according to it's related schedule and holidaycalendar.
 * If the day the class is expected to take place is a holiday, then a ClazzLog will be created but
 * it will be marked as cancelled.
 *
 * On server: create the log if the log occurs on the same day as per the timezone of the clazz. This
 * check should run every half hour.
 *
 * On client device: create the log for 24 hours from midnight as per the device time.
 *
 * @param matchLocalFromDay : this can be used on the server to require that the class log being
 * generated must be for the same day according to the timezone of the
 */
fun UmAppDatabase.createClazzLogs(fromTime: Long, toTime: Long, clazzFilter: Long = 0,
                                  matchLocalFromDay: Boolean = false) {
    val holidayCalendarHolidayLists = mutableMapOf<Long, List<Holiday>>()
    val toDateTime = DateTime.fromUnix(toTime)
    clazzDao.findClazzesWithEffectiveHolidayCalendarAndFilter(clazzFilter).forEach { clazz ->
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

        for (schedule in scheduleDao.findAllSchedulesByClazzUidAsList(clazz.clazzUid)) {
            val scheduleNextInstance = schedule.nextOccurence(effectiveTimeZone, fromTime)
            if (scheduleNextInstance >= toDateTime) {
                continue
            }

            val scheduleNextInstanceDateTimeTz = scheduleNextInstance.from.toOffsetByTimezone(
                    effectiveTimeZone)
            if (matchLocalFromDay && scheduleNextInstanceDateTimeTz.dayOfWeek != fromDayOfWeekLocal) {
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
                clazzLogDao.updateStatusByClazzLogUid(it.clazzLogUid, ClazzLog.STATUS_RESCHEDULED,
                    systemTimeInMillis())
                clazzLogAttendanceRecordDao.updateRescheduledClazzLogUids(it.clazzLogUid,
                        clazzLog.clazzLogUid, systemTimeInMillis())
            }

            if (!alreadyCreatedClazzLogs.any { it.clazzLogUid == clazzLog.clazzLogUid }) {
                clazzLogDao.insert(clazzLog)
            }
        }

    }
}