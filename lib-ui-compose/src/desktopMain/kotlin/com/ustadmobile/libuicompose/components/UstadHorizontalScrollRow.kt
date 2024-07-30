package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
actual fun UstadHorizontalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier
) {
    HorizontalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState),
        modifier = modifier
    )
}