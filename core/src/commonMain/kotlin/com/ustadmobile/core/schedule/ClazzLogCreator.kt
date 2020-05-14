package com.ustadmobile.core.schedule

import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeRange
import com.soywiz.klock.DateTimeRangeSet
import com.soywiz.klock.until
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.Holiday

/**
 * On server: create the log if the log occurs on the same day as per the timezone of the clazz
 * On client device: create the log for 24 hours from midnight as per the device time
 *
 * @param matchLocalFromDay : this can be used on the server to require that the class log being
 * generated must be for the same day according to the timezone of the
 */
fun UmAppDatabase.createClazzLogs(fromTime: Long, toTime: Long, clazzFilter: Long = 0,
    matchLocalFromDay: Boolean = false) {
    val holidayCalendarHolidayLists = mutableMapOf<Long, List<Holiday>>()
    val toDateTime = DateTime.fromUnix(toTime)
    clazzDao.findClazzesWithHolidayCalendarAndFilter(clazzFilter).forEach {clazz ->
        val alreadyCreatedClazzLogs = clazzLogDao.findByClazzUidWithinTimeRange(clazz.clazzUid,
            fromTime, toTime)

        //TODO: the timezone should fallback to using the school timezone

        val timezoneOffset = 4 * 60 * 60 * 1000
        val holCalendarUid = clazz.holidayCalendar?.umCalendarUid ?: 0L
        val clazzHolidayList = holidayCalendarHolidayLists.getOrPut(holCalendarUid) {
            holidayDao.findByHolidayCalendaUid(holCalendarUid)
        }

        for(schedule in scheduleDao.findAllSchedulesByClazzUidAsList(clazz.clazzUid)) {
            val scheduleNextInstance = schedule.nextOccurence(timezoneOffset, fromTime)
            if(scheduleNextInstance >= toDateTime) {
                continue
            }

            //TODO: Note: Now is the time to calculate the timezone offset - DST might affect this
            val applicableHolidayDateTimeRangeSet = DateTimeRangeSet(clazzHolidayList.map {
                val timeRange: DateTimeRange = DateTime.fromUnix(
                        it.holStartTime - timezoneOffset) until
                        DateTime.fromUnix(it.holEndTime - timezoneOffset)
                timeRange
            })

            //This is a holiday - stop
            if(scheduleNextInstance in applicableHolidayDateTimeRangeSet) {
                continue
            }

            val clazzLogDate = scheduleNextInstance.from.unixMillisLong
            val clazzLog = ClazzLog().apply {
                logDate = clazzLogDate
                clazzLogClazzUid = clazz.clazzUid
                clazzLogScheduleUid = schedule.scheduleUid
                clazzLogUid = generateUid()
            }

            if(!alreadyCreatedClazzLogs.any { it.clazzLogUid == clazzLog.clazzLogUid }) {
                clazzLogDao.insert(clazzLog)
            }
        }

    }
}