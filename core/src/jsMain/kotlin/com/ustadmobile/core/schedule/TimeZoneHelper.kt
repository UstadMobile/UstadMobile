package com.ustadmobile.core.schedule

import kotlin.js.Date
//date is used by js code
@Suppress("UNUSED_VARIABLE")
actual fun getTimezoneOffset(timezoneName: String, timeUtc: Long): Int {
    val date = Date(timeUtc)
    val format = js("new Intl.DateTimeFormat('en-US',{timeStyle: 'long',timeZone: timezoneName}).format(date)").toString()
    return getOffset(format)
}

//date is used by js code
@Suppress("UNUSED_VARIABLE")
actual fun getRawTimezoneOffset(timezoneName: String): Int {
    val date = Date()
    val format = js("new Intl.DateTimeFormat('en-US',{timeStyle: 'long',timeZone: timezoneName}).format(date)").toString()
    return getOffset(format) * 60 * 1000
}

//hours and minutes is used by js code
@Suppress("UNUSED_VARIABLE")
private fun getOffset(format: String): Int{
    val offset = when {
        format.indexOf("EDT") != -1 -> "-4"
        format.indexOf("EST") != -1 -> "-5"
        else -> format.substring(format.indexOf("T")+1)
    }
    val hours = offset.substringBefore(":")
    val minutes = offset.substringAfter(":","0")
    return js("(parseInt(hours) + (minutes/60)) * 60").toString().toInt()
}