package com.ustadmobile.port.android.util.compose

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.*

@Composable
fun rememberFormattedDateTime(
    timeInMillis: Long,
    timeZoneId: String,
): String  {
    val context = LocalContext.current
    return remember(timeInMillis) {
        val date = Date(timeInMillis)
        DateFormat
            .getDateFormat(context)
            .apply {
                timeZone = TimeZone.getTimeZone(timeZoneId)
            }
            .format(date) + " " +
        DateFormat.getTimeFormat(context)
            .apply {
                timeZone = TimeZone.getTimeZone(timeZoneId)
            }
            .format(date)
    }
}
