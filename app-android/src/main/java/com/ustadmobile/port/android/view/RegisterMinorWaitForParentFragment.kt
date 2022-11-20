package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentRegisterMinorWaitForParentBinding
import com.ustadmobile.core.controller.RegisterMinorWaitForParentPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.RegisterMinorWaitForParentView
import com.ustadmobile.core.viewmodel.InviteViaLinkUiState
import com.ustadmobile.core.viewmodel.RegisterMinorWaitForParentUiState
import com.ustadmobile.port.android.view.composable.UstadButton
import com.ustadmobile.port.android.view.composable.UstadButtonVariant
import com.ustadmobile.port.android.view.composable.UstadDetailField

class RegisterMinorWaitForParentFragment: UstadBaseFragment(), RegisterMinorWaitForParentView {

    var mBinding: FragmentRegisterMinorWaitForParentBinding? = null

    var mPresenter: RegisterMinorWaitForParentPresenter? = null

    override var username: String?
        get() = mBinding?.username
        set(value) {
            mBinding?.username = value
        }
    override var password: String?
        get() = mBinding?.password
        set(value) {
            mBinding?.password = value
        }

    override var parentContact: String?
        get() = mBinding?.parentContact
        set(value) {
            mBinding?.parentContact = value
        }

    override var passwordVisible: Boolean
        get() = mBinding?.passwordVisible ?: false
        set(value) {
            mBinding?.passwordVisible = value
            mBinding?.passwordToggle?.isSelected = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentRegisterMinorWaitForParentBinding.inflate(inflater, container, false)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    RegisterMinorWaitForParentScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = RegisterMinorWaitForParentPresenter(requireContext(), arguments.toStringMap(),
            this, di).withViewLifecycle()
        mBinding?.presenter = mPresenter
        mPresenter?.onCreate(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mPresenter = null
        mBinding = null
    }

}

@Composable
private fun RegisterMinorWaitForParentScreen(
    uiState: RegisterMinorWaitForParentUiState = RegisterMinorWaitForParentUiState(),
    onClickTogglePasswordVisibility: () -> Unit = {},
    onClickOk: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )  {

        val password = if(uiState.passwordVisible)
            uiState.password
        else
            "*****"

        UstadDetailField(
            valueText = uiState.username, 
            labelText = stringResource(id = R.string.username), 
            imageId = R.drawable.ic_account_circle_black_24dp
        )

        Spacer(modifier = Modifier.height(20.dp))

        UstadDetailField(
            valueText = password,
            labelText = stringResource(R.string.password),
            imageId = R.drawable.ic_baseline_vpn_key_24,
            secondaryActionContent = {
                IconButton(
                    onClick = onClickTogglePasswordVisibility,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = stringResource(id = R.string.toggle_visibility),
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = stringResource(id = R.string.we_sent_a_message_to_your_parent))

        Spacer(modifier = Modifier.height(20.dp))

        UstadButton(
            labelText = stringResource(id = R.string.ok),
            variant = UstadButtonVariant.CONTAINED,
            onClick = onClickOk
        )
    }
}

@Composable
@Preview
fun  RegisterMinorWaitForParentScreenPreview() {
    val uiStateVal = RegisterMinorWaitForParentUiState(
        username = "new.username",
        password = "secret",
        passwordVisible = false
    )

    MdcTheme {
        RegisterMinorWaitForParentScreen(
            uiState = uiStateVal
        )
    }
}