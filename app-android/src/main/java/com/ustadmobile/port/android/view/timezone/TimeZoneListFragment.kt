package com.ustadmobile.port.android.view.timezone

import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.timezone.TimeZoneListViewModel
import com.ustadmobile.libuicompose.view.timezone.TimeZoneListScreenForViewModel
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment

class TimeZoneListFragment : UstadBaseMvvmFragment() {

    val viewModel: TimeZoneListViewModel by ustadViewModels(::TimeZoneListViewModel)

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
                    TimeZoneListScreenForViewModel(viewModel)
                }
            }
        }
    }
}

