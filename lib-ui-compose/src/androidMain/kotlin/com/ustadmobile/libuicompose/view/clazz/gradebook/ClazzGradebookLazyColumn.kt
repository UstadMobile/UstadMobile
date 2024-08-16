package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import net.engawapg.lib.zoomable.ScrollGesturePropagation
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
actual fun ClazzGradebookLazyColumn(
    horizontalScrollState: ScrollState,
    lazyListState: LazyListState,
    stickyHeight: Dp,
    stickyWidth: Dp,
    scale: Float,
    modifier: Modifier,
    content: LazyListScope.() -> Unit
) {
    val zoomState = rememberZoomState()
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .zoomable(
                zoomState = zoomState,
                scrollGesturePropagation = ScrollGesturePropagation.ContentEdge
            ),
        content = content,
        state = lazyListState,
    )
}
