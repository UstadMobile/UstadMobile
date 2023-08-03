package com.ustadmobile.core.schedule

import com.ustadmobile.core.util.ext.toLocalMidnight
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.datetime.*

fun LocalDateTime.nextDayOfWeek(dayOfWeek: DayOfWeek, timeZone: TimeZone) : LocalDateTime{
    var dateTimeTmp = this

    while(dateTimeTmp.dayOfWeek != dayOfWeek) {
        dateTimeTmp = dateTimeTmp.toInstant(timeZone)
            .plus(1, DateTimeUnit.DAY, TimeZone.UTC)
            .toLocalDateTime(timeZone)
    }

    return dateTimeTmp
}

fun Schedule.nextOccurenceX(timeZone: TimeZone, fromTime: LocalDateTime): Pair<Instant, Instant> {
    //Set the time to 2am: this will ensure that it remains the same day - even if daylight savings
    // time is taking effect in between

    val startDateTime = LocalDateTime(fromTime.date, LocalTime(2, 0, 0))

    //Kotlinx DateTime uses ISO
    val nextOccurenceDay = startDateTime.nextDayOfWeek(DayOfWeek(scheduleDay), timeZone)

    val occurenceStartInstant = nextOccurenceDay.toLocalMidnight()
        .toInstant(timeZone).plus(sceduleStartTime, DateTimeUnit.MILLISECOND, timeZone)
    val occurenceFinishInstant = occurenceStartInstant.plus(
        scheduleEndTime - sceduleStartTime, DateTimeUnit.MILLISECOND, timeZone)

    return occurenceStartInstant to occurenceFinishInstant
}
