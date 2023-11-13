package com.ustadmobile.port.android.view.coursegroupset.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditViewModel
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.libuicompose.view.coursegroupset.edit.CourseGroupSetEditScreenForViewModel


class CourseGroupSetEditFragment: UstadBaseMvvmFragment(){

    private val viewModel: CourseGroupSetEditViewModel by ustadViewModels(::CourseGroupSetEditViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)
        return ComposeView(requireContext()).apply {
            setContent {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
                )

                MdcTheme {
                    CourseGroupSetEditScreenForViewModel(viewModel)
                }
            }
        }
    }
}