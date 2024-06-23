package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * On Desktop this adds vertical and horizontal scrollbars, on Android it does nothing.
 *
 * @param horizontalScrollState scroll state for horizontal scrolling
 * @param lazyListState
 * @param stickyHeight height of the sticky row (used on desktop to pad the scrollbar correctly)
 * @param stickyWidth width of the sticky column (used on desktop to pad the scrollbar correctly)
 * @param modifier
 * @param content
 */
@Composable
expect fun ClazzGradebookLazyColumn(
    horizontalScrollState: ScrollState,
    lazyListState: LazyListState,
    stickyHeight: Dp,
    stickyWidth: Dp,
    maxWidth: Dp,
    maxHeight: Dp,
    scale: Float,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
)
