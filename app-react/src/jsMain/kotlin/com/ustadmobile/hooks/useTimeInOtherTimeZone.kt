package com.ustadmobile.hooks

import com.ustadmobile.util.ext.toJsDateFromOtherTimeZoneToSystemTimeZone
import react.useMemo
import kotlin.js.Date

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
): Date? = useMemo(dependencies = arrayOf(timeInMillis, fromTimeZoneId)){
    timeInMillis.toJsDateFromOtherTimeZoneToSystemTimeZone(fromTimeZoneId)
}
