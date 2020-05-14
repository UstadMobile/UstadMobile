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

fun Schedule.nextOccurence(tzOffset: Int, after: Long = getSystemTimeInMillis()): DateTimeRange {
    val dateTimeStart = DateTime.fromUnix(after).toOffset(TimezoneOffset(tzOffset.toDouble()))
    val nextOccurenceDay = dateTimeStart.nextDayOfWeek(DayOfWeek.get(scheduleDay))
    val occurenceDayLocalMidnight = nextOccurenceDay - with(nextOccurenceDay) {
        hours.hours + minutes.minutes + + seconds.seconds + milliseconds.milliseconds
    }

    return (occurenceDayLocalMidnight.utc + sceduleStartTime.milliseconds) until (occurenceDayLocalMidnight.utc + scheduleEndTime.milliseconds)
}
