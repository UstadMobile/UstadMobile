package com.ustadmobile.port.android.view.contententry.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.libuicompose.view.contententry.edit.ContentEntryEditScreenForViewModel
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment


interface ContentEntryEdit2FragmentEventHandler {

    fun onClickUpdateContent()

    fun handleClickLanguage()

}

class ContentEntryEdit2Fragment : UstadBaseMvvmFragment(){


    private val viewModel: ContentEntryEditViewModel by ustadViewModels(::ContentEntryEditViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ContentEntryEditScreenForViewModel(viewModel)
                }
            }
        }
    }

}