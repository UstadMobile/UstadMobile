package com.ustadmobile.libuicompose.util

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import java.util.Calendar
import java.util.Date

@Composable
actual fun rememberFormattedTime(timeInMs: Int): String {
    val context = LocalContext.current

    return remember(timeInMs) {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = (timeInMs / MS_PER_HOUR)
        calendar[Calendar.MINUTE] = timeInMs.mod(MS_PER_HOUR) / MS_PER_MIN

        DateFormat
            .getTimeFormat(context)
            .format(Date(calendar.timeInMillis))
    }
}
