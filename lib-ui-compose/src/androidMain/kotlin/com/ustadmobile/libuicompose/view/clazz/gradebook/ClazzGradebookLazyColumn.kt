package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

@Composable
actual fun ClazzGradebookLazyColumn(
    horizontalScrollState: ScrollState,
    lazyListState: LazyListState,
    stickyHeight: Dp,
    stickyWidth: Dp,
    maxWidth: Dp,
    maxHeight: Dp,
    scale: Float,
    modifier: Modifier,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier
            .wrapContentSize(unbounded = true)
            .width(maxWidth / scale).height(maxHeight / scale)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        content = content,
        state = lazyListState,
    )
}
