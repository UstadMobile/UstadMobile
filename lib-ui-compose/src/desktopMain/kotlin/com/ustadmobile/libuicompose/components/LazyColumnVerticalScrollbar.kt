package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

@Composable
actual fun LazyColumnVerticalScrollbar(
    state: LazyListState,
    modifier: Modifier,
) {
    VerticalScrollbar(
        modifier = if(state.layoutInfo.reverseLayout) {
            modifier.rotate(180f)
        }else {
            modifier
        },
        adapter = rememberScrollbarAdapter(
            scrollState = state
        )
    )
}

