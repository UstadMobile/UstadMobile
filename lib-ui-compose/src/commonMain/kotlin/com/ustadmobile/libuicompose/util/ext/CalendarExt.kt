package com.ustadmobile.libuicompose.util.ext

import java.util.*

const val MS_PER_HOUR = (60 * 60 * 1000)

const val MS_PER_MIN = (60 * 1000)

/**
 * Shorthand to get the time of day (e.g. time since midnight) in milliseconds from a calendar
 * instance.
 */
val Calendar.timeOfDayInMs: Int
    get() {
        return (get(Calendar.HOUR_OF_DAY) * MS_PER_HOUR) + (get(Calendar.MINUTE) * MS_PER_MIN) +
                (get(Calendar.SECOND) * 1000) + get(Calendar.MILLISECOND)
    }
