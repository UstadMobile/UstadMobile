package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.util.ext.isDateSet
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

@Composable
actual fun rememberDateFormat(
    timeZoneId: String
): DateFormat {
    return remember(timeZoneId) {
        SimpleDateFormat.getDateInstance().also {
            it.timeZone = TimeZone.getTimeZone(timeZoneId)
        }
    }
}

@Composable
actual fun rememberFormattedDate(
    timeInMillis: Long,
    timeZoneId: String,
): String {
    return remember(timeInMillis, timeZoneId) {
        if(timeInMillis.isDateSet()) {
            SimpleDateFormat.getDateInstance().format(Date(timeInMillis))
        }else {
            ""
        }
    }
}
