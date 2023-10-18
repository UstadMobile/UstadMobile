package com.ustadmobile.libuicompose.util

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.core.util.ext.isDateSet
import java.util.Date
import java.util.TimeZone

@Composable
actual fun rememberFormattedDateTime(
    timeInMillis: Long,
    timeZoneId: String,
    joinDateAndTime: (date: String, time: String) -> String,
): String {
    val context = LocalContext.current

    return remember(timeInMillis, timeZoneId) {
        if(timeInMillis.isDateSet()) {
            val date = Date(timeInMillis)
            val dateFormatted = DateFormat
                .getDateFormat(context)
                .apply {
                    timeZone = TimeZone.getTimeZone(timeZoneId)
                }
                .format(date)
            val timeFormatted = DateFormat.getTimeFormat(context)
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
