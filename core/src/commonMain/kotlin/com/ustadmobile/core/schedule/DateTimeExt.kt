package com.ustadmobile.core.schedule

import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimezoneOffset

fun DateTime.toOffsetByTimezone(timezoneId: String): DateTimeTz {
    return toOffset(TimezoneOffset(getTimezoneOffset(timezoneId, this.unixMillisLong).toDouble()))
}

/**
 * Move the given DateTime back to what would be midnight for the given timezone.
 */
fun DateTime.toLocalMidnight(timeZoneId: String) = toOffsetByTimezone(timeZoneId).localMidnight.utc
