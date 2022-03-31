package com.ustadmobile.core.schedule

import com.ustadmobile.core.util.moment
import kotlin.js.Date
//date is used by js code
@Suppress("UNUSED_VARIABLE")
actual fun getTimezoneOffset(timezoneName: String, timeUtc: Long): Int {
    return moment(Date(timeUtc)).tz(timezoneName).utcOffset()
}

//date is used by js code
@Suppress("UNUSED_VARIABLE")
actual fun getRawTimezoneOffset(timezoneName: String): Int {
    return moment().tz(timezoneName).utcOffset() * 1000
}