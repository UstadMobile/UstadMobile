package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFormattedDateTime(
    timeInMillis: Long,
    timeZoneId: String,
    joinDateAndTime: (date: String, time: String) -> String = { date, time ->
        "$date $time"
    },
): String
