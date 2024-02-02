package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
actual fun UstadLazyVerticalGrid(
    columns: GridCells,
    modifier: Modifier,
    state: LazyGridState,
    reverseLayout: Boolean,
    verticalArrangement: Arrangement.Vertical,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = columns,
        modifier = modifier,
        state = state,
        reverseLayout = reverseLayout,
        verticalArrangement =  verticalArrangement,
        content = content,
    )
}
