package com.ustadmobile.hooks

import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import kotlinx.datetime.*
import react.useMemo

/**
 * Given a time of the day (in millseconds since midnight), generate a Javascript Date object for
 * today's date at the given time on the system default timezone.
 */
fun useTimeOfDayInMsAsJsDate(
    timeOfDayInMs: Int
) = useMemo(dependencies = arrayOf(timeOfDayInMs)) {
    val defaultTimeZone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(defaultTimeZone)
    LocalDateTime(now.date,
        LocalTime(
            hour = timeOfDayInMs/ MS_PER_HOUR,
            minute = timeOfDayInMs.mod(MS_PER_HOUR)/ MS_PER_MIN,
            second = timeOfDayInMs.mod(MS_PER_MIN) / 1000
        )
    ).toInstant(defaultTimeZone).toJSDate()
}