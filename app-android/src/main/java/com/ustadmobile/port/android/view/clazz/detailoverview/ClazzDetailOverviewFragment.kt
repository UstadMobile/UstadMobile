package com.ustadmobile.port.android.view.clazz.detailoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.libuicompose.view.clazz.detailoverview.ClazzDetailOverviewScreenForViewModel

interface ClazzDetailOverviewEventListener {
    fun onClickClassCode(code: String?)

    fun onClickShare()

    fun onClickDownloadAll()

    fun onClickPermissions()
}

class ClazzDetailOverviewFragment: UstadBaseMvvmFragment() {

    private val viewModel: ClazzDetailOverviewViewModel by ustadViewModels { di, savedStateHandle ->
        ClazzDetailOverviewViewModel(di, savedStateHandle, ClazzDetailViewModel.DEST_NAME)
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
                    ClazzDetailOverviewScreenForViewModel(viewModel)
                }
            }
        }


    }
}

