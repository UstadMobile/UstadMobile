package com.ustadmobile.libuicompose.util

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.core.util.ext.isDateSet
import java.util.Date
import java.util.TimeZone

@Composable
actual fun rememberFormattedDate(
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
