package com.ustadmobile.util.ext

import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.js.Date

/**
 * Given that the JS Date object represents a time at the system default timezone (e.g. as we get
 * from the MUI pickers), convert this into millis where it would be the same date and time of day
 * in the otherTimeZone
 *
 * See toSameDateTimeInOtherTimeZone
 */
fun Date.toMillisInOtherTimeZone(
    otherTimeZone: String,
): Long {
    return toKotlinInstant()
        .toSameDateTimeInOtherTimeZone(
            fromTimeZone = TimeZone.currentSystemDefault(),
            toTimeZone = TimeZone.of(otherTimeZone)
        ).toEpochMilliseconds()
}

/**
 * Provide the time of the day in milliseconds since midnight for the given JS Date object.
 */
fun Date.toTimeOfDayInMs(): Int {
    val dateTime = toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault())
    return (dateTime.hour * MS_PER_HOUR) + (dateTime.minute * MS_PER_MIN) + (dateTime.second * 1000)
}

/**
 * Wrapper for the setHours function that (for whatever reason) is not currently included in the
 * wrapper
 */
fun Date.setHours(
    hours: Int,
    minutes: Int,
    seconds: Int,
) {
    asDynamic().setHours(hours, minutes, seconds)
}

