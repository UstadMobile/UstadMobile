package com.ustadmobile.core.schedule

import kotlin.js.Date

actual fun getTimezoneOffset(timezoneName: String, timeUtc: Long): Int {
    val date = Date(timeUtc)
    val format = js("new Intl.DateTimeFormat('en-US',{timeStyle: 'long',timeZone: timezoneName}).format(date)").toString()
    val offset = format.substring(format.indexOf("T")+1)
    return getOffset(format)
}
actual fun getRawTimezoneOffset(timezoneName: String): Int {
    val date = Date()
    val format = js("new Intl.DateTimeFormat('en-US',{timeStyle: 'long',timeZone: timezoneName}).format(date)").toString()
    val offset = format.substring(format.indexOf("T")+1)
    return getOffset(format) * 60
}

private fun getOffset(format: String): Int{
    return when {
        format.indexOf("EDT") != -1 -> "-4"
        format.indexOf("EST") != -1 -> "-5"
        else -> format.substring(format.indexOf("T")+1)
    }.toInt()
}