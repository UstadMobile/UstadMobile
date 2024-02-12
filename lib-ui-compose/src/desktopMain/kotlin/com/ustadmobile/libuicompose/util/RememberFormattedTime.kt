package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Composable
actual fun rememberTimeFormatter(): DateFormat {
    return remember {
        SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
    }
}

@Composable
actual fun rememberFormattedTime(
    timeInMs: Int,
    formatter: DateFormat
): String {
    return remember(timeInMs) {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = (timeInMs / MS_PER_HOUR)
        calendar[Calendar.MINUTE] = timeInMs.mod(MS_PER_HOUR) / MS_PER_MIN

        formatter.format(Date(calendar.timeInMillis))
    }
}
