package com.ustadmobile.util.ext

import com.ustadmobile.mui.components.isSetDate
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJSDate
import kotlin.js.Date

/**
 * Max date that can be represented on JS
 */
val MAX_VALUE: Long
    get() = 8640000000000000L

/**
 * Where receiver represents a specific date/time in a specific from time zone (e.g.
 * 10am 2/Dec/2022 Europe/Berlin), convert this to a JSDate object that is for the same date
 * and time of day in the system default timezone (e.g.  10am 2/Dec/2022 Asia/Dubai where Asia/Dubai
 * is the system timezone).
 *
 */
internal fun Long.toJsDateFromOtherTimeZoneToSystemTimeZone(
    fromTimeZoneId: String
): Date? {
    return if(isSetDate()) {
        Instant.fromEpochMilliseconds(this)
            .toSameDateTimeInOtherTimeZone(
                fromTimeZone = TimeZone.of(fromTimeZoneId),
                toTimeZone = TimeZone.currentSystemDefault()
            ).toJSDate()
    }else {
        null
    }
}
