package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import com.ustadmobile.libuicompose.view.siteenterlink.SiteEnterLinkScreenForViewModel

class SiteEnterLinkFragment : UstadBaseMvvmFragment() {

    private val viewModel: SiteEnterLinkViewModel by ustadViewModels(::SiteEnterLinkViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    SiteEnterLinkScreenForViewModel(viewModel)
                }
            }
        }
    }

}

