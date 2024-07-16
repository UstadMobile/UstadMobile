package com.ustadmobile.core.schedule

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toLocalEndOfDay
import com.ustadmobile.core.util.ext.toLocalMidnight
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.Holiday
import kotlinx.datetime.*

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
fun UmAppDatabase.createClazzLogs(
    fromTime: Long,
    toTime: Long,
    clazzFilter: Long = 0,
    matchLocalFromDay: Boolean = false
) {
    val holidayCalendarHolidayLists = mutableMapOf<Long, List<Holiday>>()
    clazzDao().findClazzesWithEffectiveHolidayCalendarAndFilter(clazzFilter).forEach { clazz ->
        val alreadyCreatedClazzLogs = clazzLogDao().findByClazzUidWithinTimeRange(clazz.clazzUid,
                fromTime, toTime)


        val effectiveTimeZoneId = clazz.clazzTimeZone ?: "UTC"
        val effectiveTimeZone = TimeZone.of(effectiveTimeZoneId)

        val fromLocalDateTime = Instant.fromEpochMilliseconds(fromTime)
            .toLocalDateTime(effectiveTimeZone)


        val fromDayOfWeekLocalX = fromLocalDateTime.dayOfWeek

        val holCalendarUid = clazz.holidayCalendar?.umCalendarUid ?: 0L
        val clazzHolidayList = holidayCalendarHolidayLists.getOrPut(holCalendarUid) {
            holidayDao().findByHolidayCalendaUid(holCalendarUid)
        }

        for (schedule in scheduleDao().findAllSchedulesByClazzUidAsList(clazz.clazzUid)) {
            val (scheduleNextStart, scheduleNextEnd) = schedule.nextOccurenceX(effectiveTimeZone,
                Instant.fromEpochMilliseconds(fromTime).toLocalDateTime(effectiveTimeZone))
            if (scheduleNextStart.toEpochMilliseconds() >= toTime) {
                continue
            }

            val scheduleNextLocalDateTime = scheduleNextStart.toLocalDateTime(effectiveTimeZone)

            if (matchLocalFromDay && scheduleNextLocalDateTime.dayOfWeek != fromDayOfWeekLocalX) {
                continue
            }


            val holidayAndDateTimeRange = clazzHolidayList.map {
                val startInstant = Instant.fromEpochMilliseconds(it.holStartTime)
                    .toLocalDateTime(TimeZone.UTC)
                    .toLocalMidnight()
                    .toInstant(effectiveTimeZone)
                val endInstant = Instant.fromEpochMilliseconds(it.holEndTime)
                    .toLocalDateTime(TimeZone.UTC)
                    .toLocalEndOfDay()
                    .toInstant(effectiveTimeZone)

                Pair(it, Pair(startInstant, endInstant))
            }

            val overlappingHolidays = holidayAndDateTimeRange.filter {
                val (holStart, holEnd) = it.second
                holStart < scheduleNextStart && holEnd > scheduleNextEnd
            }

            val clazzLogDate = scheduleNextStart.toEpochMilliseconds()
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
                clazzLogDao().updateStatusByClazzLogUid(it.clazzLogUid, ClazzLog.STATUS_RESCHEDULED,
                    systemTimeInMillis())
                clazzLogAttendanceRecordDao().updateRescheduledClazzLogUids(it.clazzLogUid,
                        clazzLog.clazzLogUid, systemTimeInMillis())
            }

            if (!alreadyCreatedClazzLogs.any { it.clazzLogUid == clazzLog.clazzLogUid }) {
                clazzLogDao().insert(clazzLog)
            }
        }

    }
}