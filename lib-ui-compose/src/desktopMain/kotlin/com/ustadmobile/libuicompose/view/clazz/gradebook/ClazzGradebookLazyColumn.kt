package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
actual fun ClazzGradebookLazyColumn(
    horizontalScrollState: ScrollState,
    lazyListState: LazyListState,
    stickyHeight: Dp,
    stickyWidth: Dp,
    modifier: Modifier,
    content: LazyListScope.() -> Unit
) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(end = 12.dp, bottom = 12.dp),
            content = content,
            state = lazyListState,
        )

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd)
                .padding(top = stickyHeight)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = lazyListState
            )
        )

        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(start = stickyWidth)
                .fillMaxWidth(),
            adapter = rememberScrollbarAdapter(scrollState = horizontalScrollState)
        )
    }
}