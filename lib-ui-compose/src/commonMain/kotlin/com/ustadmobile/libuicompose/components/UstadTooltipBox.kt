package com.ustadmobile.libuicompose.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
expect fun UstadTooltipBox(
    tooltipText: String,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)