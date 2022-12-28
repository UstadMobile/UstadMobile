package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentLogin2Binding
import com.ustadmobile.core.controller.Login2Presenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import com.ustadmobile.core.viewmodel.LoginUiState


class Login2Fragment : UstadBaseFragment(), Login2View {

    private var mBinding: FragmentLogin2Binding? = null

    private var mPresenter: Login2Presenter? = null

    override var isEmptyPassword: Boolean = false
        set(value) {
            field = false
            mBinding?.passwordView?.isErrorEnabled = value
            if(value){
                mBinding?.passwordView?.error = getString(R.string.field_required_prompt)
            }
        }

    override var errorMessage: String = ""
        set(value) {
            field = value
            mBinding?.loginErrorText?.visibility = View.VISIBLE
            mBinding?.loginErrorText?.text = value
        }

    override var versionInfo: String? = null
        set(value) {
            field = value
            mBinding?.versionInfo = versionInfo
        }

    override var isEmptyUsername: Boolean = false
        set(value) {
            field = false
            mBinding?.usernameView?.isErrorEnabled = value
            if(value){
               mBinding?.usernameView?.error = getString(R.string.field_required_prompt)
            }
        }

    override var inProgress: Boolean = false
        set(value) {
            mBinding?.buttonEnabled = !value
            mBinding?.fieldsEnabled = !value
            mBinding?.passwordView?.isErrorEnabled = !value
            field = value
            loading = inProgress
            if(value){
                mBinding?.loginErrorText?.visibility = View.GONE
            }
        }

    override var createAccountVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.createAccount?.visibility = if(value) View.VISIBLE else View.GONE
        }

    override var connectAsGuestVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.connectAsGuest?.visibility = if(value) View.VISIBLE else View.GONE
        }

    override var loginIntentMessage: String? = null
        set(value) {
            field = value
            mBinding?.intentMessage = value
        }

    override fun clearFields() {
        mBinding?.password = ""
        mBinding?.username = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View
        mBinding = FragmentLogin2Binding.inflate(inflater, container, false).also {
            rootView = it.root
            it.buttonEnabled = true
            it.fieldsEnabled = true
        }

        return rootView
//        return ComposeView(requireContext()).apply {
//            setViewCompositionStrategy(
//                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
//            )
//
//            setContent {
//                MdcTheme {
//                    LoginScreen(uiState)
//                }
//            }
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = Login2Presenter(requireContext(), arguments.toStringMap(),this,
            di).withViewLifecycle()
        mBinding?.presenter = mPresenter
        mPresenter?.onCreate(savedInstanceState.toStringMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        mBinding = null
    }
}

@Composable
private fun LoginScreen(
    uiState: LoginUiState = LoginUiState(),
    onClickLogin: () -> Unit = {},
    onClickCreateAccount: () -> Unit = {},
    onClickConnectAsGuest: () -> Unit = {},
    onUsernameValueChange: (String) -> Unit = {},
    onPasswordValueChange: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        Text(text = uiState.loginIntentMessage ?: "")

        UstadTextEditField(
            value = uiState.username,
            label = stringResource(id = R.string.username),
            onValueChange = {
                onUsernameValueChange(it)
            },
            error = uiState.usernameError,
            enabled = uiState.fieldsEnabled,
        )

        UstadTextEditField(
            value = uiState.password,
            label = stringResource(id = R.string.password),
            onValueChange = {
                onPasswordValueChange(it)
            },
            error = uiState.passwordError,
            enabled = uiState.fieldsEnabled,
            password = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = uiState.errorMessage ?: "")

        Button(
            onClick = onClickLogin,
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.secondaryColor)
            )
        ) {
            Text(stringResource(R.string.login).uppercase(),
                color = contentColorFor(
                    colorResource(id = R.color.secondaryColor))
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onClickCreateAccount,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(stringResource(R.string.create_account).uppercase())
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onClickConnectAsGuest,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(stringResource(R.string.connect_as_guest).uppercase())
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(uiState.versionInfo)
    }
}

@Composable
@Preview
fun LoginScreenPreview() {
    MdcTheme {
        LoginScreen()
    }
}