package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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

