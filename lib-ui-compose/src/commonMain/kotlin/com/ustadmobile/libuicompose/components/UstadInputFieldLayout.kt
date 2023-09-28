package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UstadInputFieldLayout(
    modifier: Modifier = Modifier,
    errorText: String? = null,
    captionIfNoError: @Composable () -> Unit = { },
    content: @Composable () -> Unit = { },
) {
    Column(
        modifier = modifier,
    ) {
        content()
        if(errorText != null) {
            UstadErrorText(
                modifier = Modifier.padding(start = 16.dp),
                error = errorText
            )
        }else  {
            captionIfNoError()
        }
    }
}


@Composable
fun UstadTextInputLayoutPreview() {
    var text: String by remember {
        mutableStateOf("Text")
    }

    UstadInputFieldLayout(
        content = {
            OutlinedTextField(
                value = text,
                label = { Text("Field") },
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {
                    text  = it
                }
            )
        },
        errorText = "ERROR!"
    )
}
