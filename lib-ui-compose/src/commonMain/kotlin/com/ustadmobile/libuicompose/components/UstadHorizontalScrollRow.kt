package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun UstadHorizontalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
)