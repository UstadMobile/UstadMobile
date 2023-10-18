package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.util.ext.isDateSet
import java.text.SimpleDateFormat
import java.util.Date

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
