package com.ustadmobile.libuicompose.view.epubcontent

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentUiState
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentViewModel

@Composable
actual fun EpubContentScreen(
    viewModel: EpubContentViewModel
) {
    val uiState by viewModel.uiState.collectAsState(EpubContentUiState())
    EpubContentScreen(uiState)
}

/**
 * Jetpack Compose Lazy Column doesn't seem like it will work well here because
 *  1) it's affected by flickering from this issue: https://stackoverflow.com/questions/63214427/webview-flickering-android-10-tile-memory-limits-exceeded-some-content-may-no
 *
 *  2) It does not allow setting off screen item view caches which are essential for an epub to load
 *     smoothly e.g. we must load the next and previous views from the TOC before they become visible
 *     on screen
 */
@Composable
fun EpubContentScreen(
    uiState: EpubContentUiState
) {
    val recyclerViewAdapter = remember {
        EpubContentRecyclerViewAdapter()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            RecyclerView(context).apply {
                layoutManager = NoFocusScrollLinearLayoutManager(context)
                setItemViewCacheSize(2)
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                adapter = recyclerViewAdapter
            }
        }
    )

    LaunchedEffect(uiState.spineUrls) {
        recyclerViewAdapter.submitList(uiState.spineUrls)
    }

}



