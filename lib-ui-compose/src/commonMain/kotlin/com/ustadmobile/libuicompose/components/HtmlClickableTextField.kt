package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun HtmlClickableTextField(
    html: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxHtmlLines: Int = 3,
)