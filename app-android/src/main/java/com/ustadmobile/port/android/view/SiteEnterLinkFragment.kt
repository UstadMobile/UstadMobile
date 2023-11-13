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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {

                }
            }
        }
    }

}

