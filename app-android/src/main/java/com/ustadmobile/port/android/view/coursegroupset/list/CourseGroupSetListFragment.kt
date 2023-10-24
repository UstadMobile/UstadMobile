package com.ustadmobile.port.android.view.coursegroupset.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.libuicompose.view.coursegroupset.list.CourseGroupSetListScreenForViewModel
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment

class CourseGroupSetListFragment(): UstadBaseMvvmFragment(){

    private val viewModel by ustadViewModels(::CourseGroupSetListViewModel)

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
                    CourseGroupSetListScreenForViewModel(viewModel = viewModel)
                }
            }
        }
    }
}
