package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun UstadEditableHtmlField(
    html: String,
    onHtmlChange: (String) -> Unit,
    onClickToEditInNewScreen: () -> Unit,
    modifier: Modifier,
) {

    HtmlClickableTextField(
        html = html,
        label = "",
        onClick = onClickToEditInNewScreen
    )
}