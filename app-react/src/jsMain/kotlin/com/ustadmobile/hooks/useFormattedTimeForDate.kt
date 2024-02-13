package com.ustadmobile.hooks

import com.ustadmobile.wrappers.intl.Intl
import react.useMemo
import kotlin.js.Date

/**
 * Similar to useFormattedTime, but takes timeInMillis as a timestamp
 *
 * @param timeInMillis time in milliseconds (complete)
 * @param formatter DateTimeFormat
 *
 * @return The time portion of formatting the given timestamp.
 */
fun useFormattedTimeForDate(
    timeInMillis: Long,
    formatter: Intl.Companion.DateTimeFormat,
): String {
    return useMemo(dependencies = arrayOf(timeInMillis)) {
        val date = Date(timeInMillis)
        formatter.format(date)
    }
}