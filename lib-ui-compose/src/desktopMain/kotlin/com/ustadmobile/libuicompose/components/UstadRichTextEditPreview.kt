package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mohamedrejeb.richeditor.model.rememberRichTextState

@Composable
@Preview
fun UstadEditableHtmlFieldPreview(){

    var html by remember {
        mutableStateOf("")
    }
    val richTextState = rememberRichTextState()

    richTextState.setHtml(html)

    Column {

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            value = html,
            onValueChange = {  text ->
                html = text
                richTextState.setHtml(text)
            },
        )

        UstadEditableHtmlField(
            html = "Complete your assignment or <b>else</b>",
            onHtmlChange = {
                html = it
                richTextState.setHtml(it)
            }
        )
    }
}