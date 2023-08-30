package com.ustadmobile.port.android.view.contententry.list

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListUiState
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.BottomSheetOption
import com.ustadmobile.port.android.view.OptionsBottomSheetFragment
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.contententry.UstadContentEntryListItem
import com.ustadmobile.core.R as CR
import com.ustadmobile.core.MR

class ContentEntryList2Fragment : UstadBaseMvvmFragment() {

    private val viewModel by ustadViewModels { di, savedStateHandle ->
        ContentEntryListViewModel(di, savedStateHandle, requireDestinationViewName())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(
            viewModel = viewModel,
            transform = { appUiState ->
                appUiState.copy(
                    fabState = appUiState.fabState.copy(
                        onClick = this::onClickFab
                    )
                )
            }
        )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ContentEntryListScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun onClickFab() {
        val optionList = listOf(
            BottomSheetOption(
                R.drawable.ic_folder_black_24dp,
                requireContext().getString(CR.string.content_editor_create_new_category),
                42
            )
        )

        OptionsBottomSheetFragment(
            optionsList = optionList,
            onOptionSelected = {
                viewModel.onClickNewFolder()
            }
        ).show(requireActivity().supportFragmentManager, "content_list_options")
    }



    companion object {

        @JvmField
        val CONTENT_ENTRY_TYPE_ICON_MAP = mapOf(
                ContentEntry.TYPE_EBOOK to R.drawable.ic_book_black_24dp,
                ContentEntry.TYPE_VIDEO to R.drawable.video_youtube,
                ContentEntry.TYPE_DOCUMENT to R.drawable.text_doc_24px,
                ContentEntry.TYPE_ARTICLE to R.drawable.article_24px,
                ContentEntry.TYPE_COLLECTION to R.drawable.collections_24px,
                ContentEntry.TYPE_INTERACTIVE_EXERCISE to R.drawable.ic_baseline_touch_app_24,
                ContentEntry.TYPE_AUDIO to R.drawable.ic_audiotrack_24px
        )

        @JvmField
        val CONTENT_ENTRY_TYPE_LABEL_MAP = mapOf(
                ContentEntry.TYPE_EBOOK to MR.strings.ebook,
                ContentEntry.TYPE_VIDEO to MR.strings.video,
                ContentEntry.TYPE_DOCUMENT to MR.strings.document,
                ContentEntry.TYPE_ARTICLE to MR.strings.article,
                ContentEntry.TYPE_COLLECTION to MR.strings.collection,
                ContentEntry.TYPE_INTERACTIVE_EXERCISE to MR.strings.interactive,
                ContentEntry.TYPE_AUDIO to MR.strings.audio
        )

    }
}

@Composable
private fun ContentEntryListScreen(
    viewModel: ContentEntryListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ContentEntryListUiState())

    ContentEntryListScreen(
        uiState = uiState,
        onClickContentEntry = viewModel::onClickEntry
    )

}

@Composable
private fun ContentEntryListScreen(
    uiState: ContentEntryListUiState = ContentEntryListUiState(),
    onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) -> Unit = {},
    onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) -> Unit = {},
) {
    val pager = remember(uiState.contentEntryList) {
        Pager(
            pagingSourceFactory = uiState.contentEntryList,
            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
        )
    }
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()


    LazyColumn(
        modifier = Modifier.fillMaxSize()
    )  {

        items(
            items = lazyPagingItems,
            key = { contentEntry -> contentEntry.contentEntryUid }
        ){ contentEntry ->

            UstadContentEntryListItem(
                onClick = {
                    @Suppress("UNNECESSARY_SAFE_CALL")
                    contentEntry?.also { onClickContentEntry(it) }
                },
                onClickDownload = {
                    @Suppress("UNNECESSARY_SAFE_CALL")
                    contentEntry?.also { onClickDownloadContentEntry(it) }
                },
                contentEntry = contentEntry
            )
        }
    }
}

@Composable
@Preview
private fun ContentEntryListScreenPreview() {
    val uiStateVal = ContentEntryListUiState(
        contentEntryList = {
            ListPagingSource(listOf(
                ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                    contentEntryUid = 1
                    leaf = false
                    ceInactive = true
                    scoreProgress = ContentEntryStatementScoreProgress().apply {
                        progress = 10
                        penalty = 20
                    }
                    contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                    title = "Content Title 1"
                    description = "Content Description 1"
                },
                ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                    contentEntryUid = 2
                    leaf = true
                    ceInactive = false
                    contentTypeFlag = ContentEntry.TYPE_DOCUMENT
                    title = "Content Title 2"
                    description = "Content Description 2"
                }
            ))
        },
    )
    MdcTheme {
        ContentEntryListScreen(uiStateVal)
    }
}