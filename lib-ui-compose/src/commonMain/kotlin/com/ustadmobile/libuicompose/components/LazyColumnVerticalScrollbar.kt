package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun LazyColumnVerticalScrollbar(
    state: LazyListState,
    modifier: Modifier,
)
