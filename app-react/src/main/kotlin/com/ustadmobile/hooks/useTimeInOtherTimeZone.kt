package com.ustadmobile.hooks

import com.ustadmobile.util.ext.toSameDateTimeInOtherTimeZone
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJSDate
import react.useMemo

/**
 * Where timeInMillis represents a specific date/time in a specific from time zone (e.g.
 * 10am 2/Dec/2022 Europe/Berlin), convert this to a JSDate object that is for the same date
 * and time of day in the system default timezone (e.g.  10am 2/Dec/2022 Asia/Dubai where Asia/Dubai
 * is the system timezone).
 *
 * This is wrapped with the useMemo function to avoid repeating the calculation every render. This
 * is used with the MUI DatePickers, which are tied to the system default timezone.
 */
fun useTimeInOtherTimeZoneAsJsDate(
    timeInMillis: Long,
    fromTimeZoneId: String
) = useMemo(dependencies = arrayOf(timeInMillis, fromTimeZoneId)){
    Instant.fromEpochMilliseconds(timeInMillis)
        .toSameDateTimeInOtherTimeZone(
            fromTimeZone = TimeZone.of(fromTimeZoneId),
            toTimeZone = TimeZone.currentSystemDefault()
        ).toJSDate()
}
