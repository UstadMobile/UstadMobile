package com.ustadmobile.core.util

const val MS_PER_HOUR = (60 * 60 * 1000)
const val MS_PER_MIN = (60 * 1000)

fun millisSinceMidnightToHoursAndMins(millisSinceMidnight: Int): Pair<Int, Int> {
    return Pair(millisSinceMidnight / MS_PER_HOUR, millisSinceMidnight.rem(MS_PER_HOUR) / MS_PER_MIN)
}

fun hoursAndMinsToMillisSinceMidnight(hours: Int, mins: Int): Int {
    return (hours * MS_PER_HOUR) + (mins * MS_PER_MIN)
}

