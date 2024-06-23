package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
actual fun ClazzGradebookLazyColumn(
    horizontalScrollState: ScrollState,
    lazyListState: LazyListState,
    stickyHeight: Dp,
    stickyWidth: Dp,
    modifier: Modifier,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier,
        content = content,
        state = lazyListState,
    )
}
