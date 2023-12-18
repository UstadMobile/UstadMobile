package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.util.ext.isDateSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

@Composable
actual fun rememberFormattedDateTime(
    timeInMillis: Long,
    timeZoneId: String,
    joinDateAndTime: (date: String, time: String) -> String,
): String {
    return remember(timeInMillis, timeZoneId) {
        if(timeInMillis.isDateSet()) {
            val date = Date(timeInMillis)
            val dateFormatted = SimpleDateFormat
                .getDateInstance()
                .apply {
                    timeZone = TimeZone.getTimeZone(timeZoneId)
                }
                .format(date)

            val timeFormatted = SimpleDateFormat.getTimeInstance()
                .apply {
                    timeZone = TimeZone.getTimeZone(timeZoneId)
                }
                .format(date)

            joinDateAndTime(dateFormatted, timeFormatted)
        }else {
            ""
        }
    }
}