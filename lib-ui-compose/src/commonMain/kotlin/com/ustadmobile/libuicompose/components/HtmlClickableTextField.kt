package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box

@Composable
expect fun HtmlClickableTextField(
    root: @Composable (() -> Unit),
    html: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxHtmlLines: Int = 3,
)