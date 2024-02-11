package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun UstadTooltipBox(
    tooltipText: String,
    textColor: Color,
    modifier: Modifier,
    content: @Composable () -> Unit,
)  {
    Box(
        modifier = modifier,
    ) {
        content()
    }
}