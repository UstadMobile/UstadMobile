package com.ustadmobile.libuicompose.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.ustadmobile.core.util.MS_PER_HOUR

@Preview
@Composable
fun UstadTimeFieldPreview(){

    var time: Int by remember {
        mutableStateOf(10 * MS_PER_HOUR)
    }

    UstadTimeField(
        value = time,
        label = { Text("Time") },
        onValueChange = {
            time = it
        }
    )


}