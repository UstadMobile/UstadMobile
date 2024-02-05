package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun UstadLazyVerticalGrid(
    columns: GridCells,
    modifier: Modifier,
    state: LazyGridState,
    reverseLayout: Boolean,
    verticalArrangement: Arrangement.Vertical,
    content: LazyGridScope.() -> Unit,
) {
    Box(
        modifier = modifier
    ) {
        LazyVerticalGrid(
            columns = columns,
            state = state,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            modifier = Modifier.fillMaxSize().padding(end = 12.dp),
            content = content,
        )

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = state
            )
        )
    }

}