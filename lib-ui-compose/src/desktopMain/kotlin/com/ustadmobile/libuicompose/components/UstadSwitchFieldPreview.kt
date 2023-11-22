package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
@Preview
fun UstadSwitchRowPreview() {
    var switchRow: Boolean by remember {
        mutableStateOf(true)
    }

    UstadSwitchField(
        checked = switchRow,
        label = "Switch",
        onChange = {
            switchRow = !switchRow
        }
    )
}