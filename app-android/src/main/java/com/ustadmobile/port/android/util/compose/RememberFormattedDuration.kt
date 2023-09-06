package com.ustadmobile.port.android.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.pluralStringResource
import com.ustadmobile.core.R as CR
import com.ustadmobile.port.android.util.ext.MS_PER_HOUR
import com.ustadmobile.port.android.util.ext.MS_PER_MIN

/**
 * Create a human readable string for a duration. E.g. "1 hour 49 minutes"
 *
 * @param timeInMillis the duration in milliseconds
 * @return formatted string e.g. 1 hour 49 minutes
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberFormattedDuration(
    timeInMillis: Long,
): String {

    val hours = (timeInMillis / MS_PER_HOUR)
    val mins = timeInMillis.mod(MS_PER_HOUR) / MS_PER_MIN
    val hoursStr = pluralStringResource(id = CR.plurals.duration_hours, count = hours.toInt())
    val minsStr = pluralStringResource(id = CR.plurals.duration_minutes, count = mins)

    return remember(timeInMillis) {
        var str = ""
        if(hours > 0)
            str += "$hoursStr "

        if(mins > 0)
            str += minsStr

        str
    }
}

