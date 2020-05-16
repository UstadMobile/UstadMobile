package com.ustadmobile.core.schedule
import java.util.TimeZone

actual fun getTimezoneOffset(timezoneName: String, timeUtc: Long): Int {
    return TimeZone.getTimeZone(timezoneName).getOffset(timeUtc)
}

actual fun getRawTimezoneOffset(timezoneName: String): Int {
    return TimeZone.getTimeZone(timezoneName).rawOffset
}
