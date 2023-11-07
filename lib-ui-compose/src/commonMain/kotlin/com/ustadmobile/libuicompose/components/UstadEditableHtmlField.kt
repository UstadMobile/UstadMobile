package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun UstadEditableHtmlField(
    html: String,
    onHtmlChange: (String) -> Unit = {},
    onClickToEditInNewScreen: () -> Unit = {},
    modifier: Modifier = Modifier,
)