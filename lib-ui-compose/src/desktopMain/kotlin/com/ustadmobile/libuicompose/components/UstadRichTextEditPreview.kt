package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
@Preview
fun UstadEditableHtmlFieldPreview(){

    var html by remember {
        mutableStateOf("Complete your assignment or <b>else</b>")
    }

    UstadRichTextEdit(
        html = html,
        onHtmlChange = {
            html = it
        }
    )
}