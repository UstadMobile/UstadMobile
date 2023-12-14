package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    htmlMaxLines: Int = Int.MAX_VALUE,
)