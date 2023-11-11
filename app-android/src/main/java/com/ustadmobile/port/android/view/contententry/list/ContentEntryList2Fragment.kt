package com.ustadmobile.port.android.view.contententry.list

import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.BottomSheetOption
import com.ustadmobile.port.android.view.OptionsBottomSheetFragment
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.core.R as CR
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.view.contententry.list.ContentEntryListScreenForViewModel

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
                    ContentEntryListScreenForViewModel(viewModel = viewModel)
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