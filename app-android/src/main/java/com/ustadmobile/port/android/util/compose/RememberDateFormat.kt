package com.ustadmobile.port.android.util.compose

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.*

/**
 * Format the given date. Use the remember function for performance enhancement. Date formatting can
 * use up CPU cycles.
 *
 * The remember function will use the time in millis as the key, so if the value is changed, it will
 * get invalidated.
 */
@Composable
fun rememberFormattedDate(
    timeInMillis: Long
): String {
    val context = LocalContext.current
    return remember(timeInMillis) {
        DateFormat
            .getDateFormat(context)
            .format(Date(timeInMillis))
    }
}
