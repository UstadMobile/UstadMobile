package com.ustadmobile.hooks

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.wrappers.intl.Intl
import com.ustadmobile.util.ext.toJsDateFromOtherTimeZoneToSystemTimeZone
import react.useMemo

/**
 * Format and display the date for a given time in millis using the display locale. This will try
 * to use Intl (available in most modern browsers), but will then fallback to using the standard
 * Javascript Date.toDateString
 *
 * @param timeInMillis time since epoch in ms
 * @param timezoneId the timezone to use to calculate the date
 */
fun useFormattedDate(timeInMillis: Long, timezoneId: String) : String{
    return useMemo(dependencies = arrayOf(timeInMillis, timezoneId)) {
        val dateOffsetForTimezone = timeInMillis.toJsDateFromOtherTimeZoneToSystemTimeZone(timezoneId)
        try {
            dateOffsetForTimezone?.let {
                Intl.Companion.DateTimeFormat(UstadMobileSystemImpl.displayedLocale).format(it)
            } ?: ""
        }catch (e: Exception) {
            dateOffsetForTimezone?.toDateString() ?: ""
        }
    }
}

/**
 * Format a start and end date e.g. "01/Jan/20 - 15/Jan/20". Usefor for enrolments, class end dates,
 * etc.
 *
 * @param startTimeInMillis the start date
 * @param endTimeInMillis the end date
 * @param timezoneId the timezone to use to calculate the dates
 */
fun useFormattedDateRange(
    startTimeInMillis: Long,
    endTimeInMillis: Long,
    timezoneId: String,
    joiner: (String, String) -> String = {start, end -> "$start - $end"}
): String {
    val startDate = useFormattedDate(startTimeInMillis, timezoneId)
    val endDate = useFormattedDate(endTimeInMillis, timezoneId)
    return useMemo(
        dependencies = arrayOf(startTimeInMillis, endTimeInMillis, timezoneId)
    ) {
        joiner(startDate, endDate)
    }
}
