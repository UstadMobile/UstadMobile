package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import dev.icerock.moko.resources.compose.stringResource

/**
 * Create a human readable string for a duration. E.g. "1 hour 49 minutes". For the moment this
 * does not really handle plurals.
 *
 * @param timeInMillis the duration in milliseconds
 * @return formatted string e.g. 1 hour 49 minutes
 */
@Composable
fun rememberFormattedDuration(timeInMillis: Long) : String {
    val hoursStr = stringResource(MR.strings.xapi_hours)
    val minsStr = stringResource(MR.strings.xapi_minutes)
    val secsStr = stringResource(MR.strings.xapi_seconds)

    return remember(timeInMillis) {
        val hours = (timeInMillis / MS_PER_HOUR)
        val mins = timeInMillis.mod(MS_PER_HOUR) / MS_PER_MIN
        val secs = (timeInMillis % MS_PER_MIN) / 1000

        buildString {
            if(hours > 0)
                append("$hours $hoursStr ")

            if(mins > 0)
                append("$mins $minsStr")

            if (secs > 0 || (hours.toInt() == 0 && mins == 0))
                append("$secs $secsStr")
        }
    }
}
