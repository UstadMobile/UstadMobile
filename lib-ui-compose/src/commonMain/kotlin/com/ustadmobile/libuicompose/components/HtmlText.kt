package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun UstadHtmlText(
    html: String,
    modifier: Modifier = Modifier,
    htmlMaxLines: Int = Int.MAX_VALUE,
)