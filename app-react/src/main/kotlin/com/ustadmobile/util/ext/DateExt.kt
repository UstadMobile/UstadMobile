package com.ustadmobile.util.ext

import com.ustadmobile.util.moment
import kotlin.js.Date

fun Date.getUtcTime(): Long {
    return this.getTime().toLong() - getLocalOffset()
}

//date is used by js code
@Suppress("UNUSED_VARIABLE")
fun Date.getDateFormat(): String {
    val date = this
    val timezoneName = js("Intl.DateTimeFormat().resolvedOptions().timeZone")
    return js("new Intl.DateTimeFormat('en-US',{timeStyle: 'long',timeZone: timezoneName}).format(date)").toString()
}

fun Date.getLocalOffset(): Long {
    val format = getDateFormat()
    return when {
        format.indexOf("EDT") != -1 -> "-4"
        format.indexOf("EST") != -1 -> "-5"
        else -> format.substring(format.indexOf("T")+1)
    }.toLong() * 3600000
}

fun Date.formatDate(format: String = "DD/MMMM/YYYY"): String {
    val utc = moment.utc(this).toDate()
    return moment(utc).local().format(format)
}