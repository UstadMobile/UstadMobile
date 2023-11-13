package com.ustadmobile.port.android.view.discussionpost.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.libuicompose.view.discussionpost.detail.DiscussionPostDetailScreenForViewModel
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment


class DiscussionPostDetailFragment: UstadBaseMvvmFragment() {

    private val viewModel: DiscussionPostDetailViewModel by ustadViewModels{ di, savedStateHandle ->
        DiscussionPostDetailViewModel(di, savedStateHandle, requireDestinationViewName())
    }

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
                    DiscussionPostDetailScreenForViewModel(viewModel)
                }
            }
        }
    }


    companion object {
        //If anything..
    }

}