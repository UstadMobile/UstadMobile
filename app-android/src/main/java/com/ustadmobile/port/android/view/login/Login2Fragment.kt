package com.ustadmobile.port.android.view.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.libuicompose.view.login.LoginScreenForViewModel
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment

class Login2Fragment : UstadBaseMvvmFragment() {

    private val viewModel: LoginViewModel by ustadViewModels(::LoginViewModel)

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
                    LoginScreenForViewModel(viewModel)
                }
            }
        }
    }
}
