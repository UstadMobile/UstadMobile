package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable

/**
 * timeInMs should be the hours, minutes, and seconds multiplied out into milliseconds.
 *
 * This will return a string that uses the locale's time formatter. This might be a 24hour clock,
 * or am/pm depending on the locale.
 *
 */
@Composable
expect fun rememberFormattedTime(timeInMs: Int): String
