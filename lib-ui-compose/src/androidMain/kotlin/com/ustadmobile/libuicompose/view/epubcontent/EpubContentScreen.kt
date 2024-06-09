package com.ustadmobile.libuicompose.view.epubcontent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.compose.AsyncImage
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentUiState
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentViewModel
import com.ustadmobile.core.viewmodel.epubcontent.EpubScrollCommand
import com.ustadmobile.core.viewmodel.epubcontent.EpubTocItem
import com.ustadmobile.libuicompose.components.LifecycleActiveEffect
import kotlinx.coroutines.flow.Flow
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance

@Composable
actual fun EpubContentScreen(
    viewModel: EpubContentViewModel
) {
    val uiState by viewModel.uiState.collectAsState(EpubContentUiState())
    EpubContentScreen(
        uiState = uiState,
        onDismissTableOfContents = viewModel::onDismissTableOfContentsDrawer,
        onClickTocItem = viewModel::onClickTocItem,
        scrollCommandFlow = viewModel.epubScrollCommands,
        onClickLink = {
            viewModel.onClickLink(it, it) //On Android it (the href) is already absolute
        },
        onSpineIndexChanged = viewModel::onSpineIndexChanged,
        onActiveChanged = viewModel::onActiveChanged,
    )
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
    uiState: EpubContentUiState,
    onDismissTableOfContents: () -> Unit,
    onClickTocItem: (EpubTocItem) -> Unit,
    onClickLink: (String) -> Unit,
    scrollCommandFlow: Flow<EpubScrollCommand>,
    onActiveChanged: (Boolean) -> Unit = { },
    onSpineIndexChanged: (Int) -> Unit = { },
) {
    val di = localDI()
    val contentEntryVersionServer: ContentEntryVersionServerUseCase = remember {
        di.onActiveEndpoint().direct.instance()
    }

    val recyclerViewAdapter = remember(uiState.contentEntryVersionUid) {
        EpubContentRecyclerViewAdapter(
            contentEntryVersionServer = contentEntryVersionServer,
            xmlPullParserFactory = di.direct.instance(tag = DiTag.XPP_FACTORY_NSAWARE),
            contentEntryVersionUid = uiState.contentEntryVersionUid,
            scrollCommandFlow = scrollCommandFlow,
            onClickLink = onClickLink,
        )
    }

    var recyclerViewRef: RecyclerView? by remember {
        mutableStateOf(null)
    }

    var recyclerViewLayoutRef: LinearLayoutManager? by remember {
        mutableStateOf(null)
    }

    LifecycleActiveEffect(onActiveChanged)

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed,
    )

    LaunchedEffect(uiState.tableOfContentsOpen) {
        drawerState.apply {
            if(uiState.tableOfContentsOpen)
                open()
            else
                close()
        }
    }

    LaunchedEffect(uiState.tableOfContentsOpen, drawerState.isOpen, drawerState.isAnimationRunning) {
        if(!drawerState.isAnimationRunning) {
            if(uiState.tableOfContentsOpen && !drawerState.isOpen)
                onDismissTableOfContents()
        }
    }

    var coverImageRequestUrl: String? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(uiState.coverImageUrl) {
        val pathInManifest = uiState.coverImageUrl?.substringAfter(
            "/api/content/${uiState.contentEntryVersionUid}/"
        )

        coverImageRequestUrl = if(pathInManifest != null) {
            contentEntryVersionServer.getManifestEntry(
                contentEntryVersionUid = uiState.contentEntryVersionUid,
                pathInContentEntryVersion =  pathInManifest
            )?.bodyDataUrl
        }else {
            null
        }
    }

    LaunchedEffect(scrollCommandFlow) {
        scrollCommandFlow.collect {
            recyclerViewLayoutRef?.scrollToPositionWithOffset(it.spineIndex, 0)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item("_ustadepub_cover_img") {
                        val coverImageUrl = coverImageRequestUrl
                        if(coverImageUrl != null) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    modifier = Modifier.height(200.dp),
                                    model = coverImageUrl,
                                    contentScale = ContentScale.Fit,
                                    contentDescription = null
                                )
                            }
                        }else {
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    items(
                        uiState.tableOfContentToDisplay, key = { it.uid }
                    ) { tocItem ->
                        NavigationDrawerItem(
                            label = { Text(tocItem.label) },
                            onClick = {
                                onDismissTableOfContents()
                                onClickTocItem(tocItem)
                            },
                            selected = false,
                        )
                    }
                }
            }
        },
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                RecyclerView(context).apply {
                    layoutManager = NoFocusScrollLinearLayoutManager(context).also {
                        recyclerViewLayoutRef = it
                    }
                    setItemViewCacheSize(2)
                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                    adapter = recyclerViewAdapter
                    recyclerViewRef = this
                    addOnScrollListener(
                        object: RecyclerView.OnScrollListener() {
                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                val layoutManager = recyclerView.layoutManager
                                        as? LinearLayoutManager ?: return
                                onSpineIndexChanged(layoutManager.findLastVisibleItemPosition())
                            }
                        }
                    )
                }
            },
            update = {
                if(it.adapter !== recyclerViewAdapter)
                    it.adapter = recyclerViewAdapter
            }
        )
    }

    LaunchedEffect(uiState.spineUrls) {
        recyclerViewAdapter.submitList(uiState.spineUrls)
    }
}



