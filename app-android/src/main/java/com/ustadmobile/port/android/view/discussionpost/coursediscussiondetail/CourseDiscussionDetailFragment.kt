package com.ustadmobile.port.android.view.discussionpost.coursediscussiondetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailViewModel
import com.ustadmobile.libuicompose.view.discussionpost.coursediscussiondetail.CourseDiscussionDetailScreenForViewModel

class CourseDiscussionDetailFragment: UstadBaseMvvmFragment() {

    val viewModel by ustadViewModels(::CourseDiscussionDetailViewModel)

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
                    CourseDiscussionDetailScreenForViewModel(viewModel)
                }
            }
        }
    }

}