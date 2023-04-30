package com.ustadmobile.port.android.util.compose

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.core.util.ext.isDateSet
import java.util.*

/**
 * Format the given date. Use the remember function for performance enhancement. Date formatting can
 * use up CPU cycles.
 *
 * The remember function will use the time in millis as the key, so if the value is changed, it will
 * get invalidated.
 */
@Composable
fun rememberFormattedDate(
    timeInMillis: Long,
    timeZoneId: String,
): String {
    val context = LocalContext.current
    return remember(timeInMillis, timeZoneId) {
        if(timeInMillis.isDateSet()) {
            DateFormat
                .getDateFormat(context)
                .apply {
                    timeZone = TimeZone.getTimeZone(timeZoneId)
                }
                .format(Date(timeInMillis))
        }else {
            ""
        }
    }
}

/**
 * Format a start date - to date string e.g. "01/Jan/20 - 15/Jan/20". Usefor for enrolments, class
 * end dates, etc.
 */
@Composable
fun rememberFormattedDateRange(
    startTimeInMillis: Long,
    endTimeInMillis: Long,
    timeZoneId: String,
    joiner: (String, String) -> String = {start, end -> "$start - $end"}
): String {
    val startDate = rememberFormattedDate(startTimeInMillis, timeZoneId)
    val endDate = rememberFormattedDate(endTimeInMillis, timeZoneId = timeZoneId)
    return remember(startTimeInMillis, endTimeInMillis, timeZoneId) {
        joiner(startDate, endDate)
    }
}
