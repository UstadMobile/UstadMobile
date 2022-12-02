package com.ustadmobile.util.ext

import com.ustadmobile.core.util.moment
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlin.js.Date

const val TIME_FORMAT_H_M = "HH:mm"

const val DATE_FORMAT_DD_MMM_YYYY_HM = "DD MMM YYYY - $TIME_FORMAT_H_M"

const val DATE_FORMAT_DD_MMM_YYYY = "DD MMM YYYY"

const val DATE_FORMAT_DDDD_MMMM_DD_H_M = "dddd, MMMM DD $TIME_FORMAT_H_M"

const val DATE_FORMAT_DD_MM_YYYY = "DD/MM/YYYY"

const val DATE_FORMAT_MM_YYYY = "MM/YYYY"

const val DATE_FORMAT_MMMM_DD_YYYY = "MMMM DD, YYYY"

fun Date.formatDate(format: String? = DATE_FORMAT_DD_MM_YYYY, timezone: String? = null): String {
    val utc = moment.utc(this).toDate()
    return moment(utc).utcOffset(timezone ?: "").format(format) as String
}

fun Date.standardFormat(timezone: String? = null): String = formatDate(DATE_FORMAT_DD_MM_YYYY, timezone)

fun Date.fullDateFormat(format: String = DATE_FORMAT_MMMM_DD_YYYY, timezone: String? = null)
: String = formatDate(format, timezone)

fun Date.formattedInHoursAndMinutes(timezone: String? = null) : String = formatDate(TIME_FORMAT_H_M, timezone)

fun Date.formattedWithFullMonth(timezone: String? = null): String = formatDate(DATE_FORMAT_DD_MM_YYYY, timezone)

fun Date.formatFullDate(timezone: String? = null): String = formatDate(DATE_FORMAT_DDDD_MMMM_DD_H_M, timezone)

fun Date.startOfDay(timezone: String? = null) : Date{
    val utc = moment.utc(this).toDate()
    return moment(utc).utcOffset(timezone ?: "").startOf("day").toDate()
}

fun Date.fromNow(locale: String = "en",withSuffix: Boolean = true): String{
    moment.locale(locale)
    val utc = moment.utc(this).toDate()
    return moment(utc).fromNow(!withSuffix).toString()
}

fun Date.endOfDay(timezone: String? = null) : Date{
    val utc = moment.utc(this).toDate()
    return moment(utc).utcOffset(timezone ?: "").endOf("day").toDate()
}

fun Date.timeInMillsFromStartOfDay(timezone: String? = null): Long {
    return (this.getTime() - this.startOfDay(timezone).getTime()).toLong()
}

fun Date.addDays(days: Int, timezone: String? = null): Date {
    val utc = moment.utc(this).toDate()
    return moment(utc).utcOffset(timezone ?: "").add(days, "days").toDate()
}

/**
 * Given that the JS Date object represents a time at the system default timezone (e.g. as we get
 * from the MUI pickers), convert this into millis where it would be the same date and time of day
 * in the otherTimeZone
 *
 * See toSameDateTimeInOtherTimeZone
 */
fun Date.toMillisInOtherTimeZone(
    otherTimeZone: String,
): Long {
    return toKotlinInstant()
        .toSameDateTimeInOtherTimeZone(
            fromTimeZone = TimeZone.currentSystemDefault(),
            toTimeZone = TimeZone.of(otherTimeZone)
        ).toEpochMilliseconds()
}
