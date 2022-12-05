package com.ustadmobile.port.android.util.compose

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.port.android.util.ext.MS_PER_HOUR
import com.ustadmobile.port.android.util.ext.MS_PER_MIN
import java.util.*

/**
 * timeInMs should be the hours, minutes, and seconds multiplied out into MS. It should not include
 */
@Composable
fun rememberFormattedTime(timeInMs: Int): String {
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

