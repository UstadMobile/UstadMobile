package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun UstadRichTextEdit(
    html: String,
    onHtmlChange: (String) -> Unit,
    onClickToEditInNewScreen: () -> Unit,
    modifier: Modifier,
    editInNewScreen: Boolean,
    editInNewScreenLabel: String?,
    placeholderText: String?,
) {
    if(editInNewScreen) {
        HtmlClickableTextField(
            html = html,
            label = editInNewScreenLabel ?: "",
            onClick = onClickToEditInNewScreen,
            modifier = modifier
        )
    }else {
        Box(
            modifier = Modifier.imePadding()
                .fillMaxSize()
        ) {
            AztecEditor(
                modifier = modifier,
                html = html,
                onChange = onHtmlChange,
                placeholderText = placeholderText,
            )
        }

    }
}