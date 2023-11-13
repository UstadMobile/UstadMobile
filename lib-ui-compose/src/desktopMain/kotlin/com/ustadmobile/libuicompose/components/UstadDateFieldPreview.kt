package com.ustadmobile.libuicompose.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Preview
@Composable
fun UstadDateFieldPreview(){
    var date by remember {
        mutableStateOf((0).toLong())
    }
    UstadDateField(
        value = date,
        label = { Text( "Date") },
        timeZoneId = "UTC",
        onValueChange = {
            date = it
        }
    )
}