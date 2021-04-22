package com.ustadmobile.core.schedule

import com.soywiz.klock.*
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.util.getSystemTimeInMillis

fun DateTimeTz.nextDayOfWeek(dayOfWeek: DayOfWeek): DateTimeTz {
    var dateTimeTmp = this

    while(dateTimeTmp.dayOfWeek != dayOfWeek) {
        dateTimeTmp += 1.days
    }

    return dateTimeTmp
}

fun Schedule.nextOccurence(timezoneName: String, after: Long = getSystemTimeInMillis()): DateTimeRange {
    //Set the time to 2am: this will ensure that it remains the same day - even if daylight savings
    // time is taking effect in between
    val rawTzOffset = getRawTimezoneOffset(timezoneName)
    val dateTimeStart = DateTime.fromUnix(after).toOffset(TimezoneOffset(rawTzOffset.toDouble())).localMidnight +
            2.hours

    val nextOccurenceDay = dateTimeStart.nextDayOfWeek(DayOfWeek.get(scheduleDay))

    val daylightSavingsDelta = rawTzOffset - getTimezoneOffset(timezoneName, nextOccurenceDay.utc.unixMillisLong)
    val occurenceDayLocalMidnight = nextOccurenceDay.localMidnight

    return (occurenceDayLocalMidnight.utc + sceduleStartTime.milliseconds + daylightSavingsDelta.milliseconds) until
            (occurenceDayLocalMidnight.utc + scheduleEndTime.milliseconds + daylightSavingsDelta.milliseconds)
}

fun List<Schedule>.nextOccurence(timezoneName: String, after: Long = getSystemTimeInMillis()): DateTimeRange? {
    return map { it.nextOccurence(timezoneName, after) }.minByOrNull { it.from.unixMillisLong }
}

val Schedule.duration: Int
    get() = (scheduleEndTime - sceduleStartTime).toInt()
