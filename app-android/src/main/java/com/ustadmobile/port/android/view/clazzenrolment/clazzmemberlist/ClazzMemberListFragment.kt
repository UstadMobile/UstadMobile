package com.ustadmobile.port.android.view.clazzenrolment.clazzmemberlist

import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.libuicompose.view.clazzenrolment.clazzmemberlist.ClazzMemberListScreenForViewModel


class ClazzMemberListFragment() : UstadBaseMvvmFragment() {

    private val viewModel: ClazzMemberListViewModel by ustadViewModels(::ClazzMemberListViewModel)

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
                    ClazzMemberListScreenForViewModel(viewModel)
                }
            }
        }
    }

}

