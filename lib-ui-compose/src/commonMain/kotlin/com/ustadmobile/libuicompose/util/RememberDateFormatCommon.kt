package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

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
