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

/**
 * Get the age in years for a given DateTime as an int. This has to consider leap years etc, so
 * simple division does not work.
 */
fun DateTime.age() : Int{
    val dateNow = DateTime.now()
    var age = dateNow.yearInt - this.yearInt
    if(this.month0 < dateNow.month0) {
        age--
    }else if(this.month0 == dateNow.month0 && this.dayOfYear > dateNow.dayOfYear) {
        age--
    }

    return age
}
