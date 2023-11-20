package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import java.util.TimeZone

@Preview
@Composable
fun UstadDateTimeFieldPreview() {
    UstadDateTimeField(
        value = System.currentTimeMillis(),
        dateLabel =  { Text("Date") },
        timeLabel = { Text("Time") },
        timeZoneId = TimeZone.getDefault().id
    )
}