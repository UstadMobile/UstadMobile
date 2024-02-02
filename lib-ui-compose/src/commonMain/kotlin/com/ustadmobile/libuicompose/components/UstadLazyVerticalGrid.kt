package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Same as UstadLazyVerticalColumn - simply provides the vertical scrollbar on desktop. No effect on
 * Android.
 */
@Composable
expect fun UstadLazyVerticalGrid(
    columns: GridCells,
    modifier: Modifier,
    state: LazyGridState = rememberLazyGridState(),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    content: LazyGridScope.() -> Unit,
)