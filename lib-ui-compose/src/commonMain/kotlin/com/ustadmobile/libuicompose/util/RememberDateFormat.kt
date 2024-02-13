package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import java.text.DateFormat

@Composable
expect fun rememberDateFormat(
    timeZoneId: String,
): DateFormat

/**
 * Format the given date. Use the remember function for performance enhancement. Date formatting can
 * use up CPU cycles.
 *
 * The remember function will use the time in millis as the key, so if the value is changed, it will
 * get invalidated.
 */
@Composable
expect fun rememberFormattedDate(
    timeInMillis: Long,
    timeZoneId: String,
): String

