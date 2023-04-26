package com.ustadmobile.core.util.ext

import kotlinx.datetime.*

fun LocalDateTime.toLocalMidnight() : LocalDateTime {
    return LocalDateTime(date, LocalTime(0, 0, 0))
}

fun LocalDateTime.toLocalEndOfDay() : LocalDateTime {
    return LocalDateTime(date, LocalTime(23, 59, 59))
}

fun LocalDateTime.ageInYears(): Int {
    val dateTimePeriod: DateTimePeriod = toInstant(TimeZone.UTC)
        .periodUntil(Clock.System.now(), TimeZone.UTC)
    return dateTimePeriod.years
}

fun LocalDateTime.chopOffSeconds() : LocalDateTime {
    if(second > 0 || nanosecond > 0) {
        return LocalDateTime(date, LocalTime(hour, minute))
    }else {
        return this
    }
}

