package com.ustadmobile.util.ext

import com.ustadmobile.core.util.moment
import kotlin.js.Date

fun Date.formatDate(format: String = "DD/MM/YYYY", timezone: String? = null): String {
    val utc = moment.utc(this).toDate()
    return moment(utc).utcOffset(timezone ?: "").format(format) as String
}

fun Date.maxDate() = Date(8640000000000000)

fun Date.standardFormat(timezone: String? = null): String = formatDate("DD/MM/YYYY", timezone)

fun Date.fullDateFormat(format: String = "MMMM DD, YYYY", timezone: String? = null)
: String = formatDate(format, timezone)

fun Date.formattedInHoursAndMinutes(timezone: String? = null) : String = formatDate("HH:mm", timezone)

fun Date.formattedWithFullMonth(timezone: String? = null): String = formatDate("DD/MMMM/YYYY", timezone)

fun Date.formatFullDate(timezone: String? = null): String = formatDate("dddd, MMMM DD h:m", timezone)

fun Date.startOfDay(timezone: String? = null) : Date{
    val utc = moment.utc(this).toDate()
    return moment(utc).utcOffset(timezone ?: "").startOf("day").toDate()
}

fun Date.endOfDay(timezone: String? = null) : Date{
    val utc = moment.utc(this).toDate()
    return moment(utc).utcOffset(timezone ?: "").endOf("day").toDate()
}

fun Date.timeInMillsFromStartOfDay(timezone: String? = null): Long {
    return (this.getTime() - this.startOfDay(timezone).getTime()).toLong()
}