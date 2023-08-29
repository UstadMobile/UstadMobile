package com.ustadmobile.port.android.view.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.login.LoginUiState
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.libuicompose.view.login.LoginScreen


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

@Composable
private fun LoginScreenForViewModel(
    viewModel: LoginViewModel
) {
    val uiState: LoginUiState by viewModel.uiState.collectAsState(LoginUiState())
    LoginScreen(
        uiState = uiState,
        onClickLogin = viewModel::onClickLogin,
        onClickCreateAccount = viewModel::onClickCreateAccount,
        onClickConnectAsGuest = viewModel::handleConnectAsGuest,
        onUsernameValueChange = viewModel::onUsernameChanged,
        onPasswordValueChange = viewModel::onPasswordChanged,
    )
}

@Composable
@Preview
fun LoginScreenPreview() {
    MdcTheme {
        LoginScreen()
    }
}