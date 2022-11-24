package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPersonAccountEditBinding
import com.ustadmobile.core.controller.PersonAccountEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.core.view.PersonAccountEditView.Companion.BLOCK_CHARACTER_SET
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import com.ustadmobile.port.android.view.util.ClearErrorTextWatcher
import com.ustadmobile.core.viewmodel.PersonAccountEditUiState


class PersonAccountEditFragment: UstadEditFragment<PersonWithAccount>(), PersonAccountEditView {

    private var mBinding: FragmentPersonAccountEditBinding? = null

    private var mPresenter: PersonAccountEditPresenter? = null

    override var currentPasswordError: String?
        set(value) {
            mBinding?.currentPasswordError = value
        }
        get() = mBinding?.currentPasswordError


    override var newPasswordError: String?
        set(value) {
            mBinding?.passwordError = value
        }
        get() = mBinding?.passwordError


    override var confirmedPasswordError: String?
        set(value) {
            mBinding?.passwordConfirmError = value
        }
        get() = mBinding?.passwordConfirmError

    override var noPasswordMatchError: String?

        set(value) {
            mBinding?.passwordError = value
            mBinding?.passwordConfirmError = value
        }
        get() = mBinding?.passwordError


    override var usernameError: String?
        set(value) {
            mBinding?.usernameError = value
        }
        get() = mBinding?.usernameError


    override var errorMessage: String? = null
        set(value) {
            field = value
            mBinding?.errorText?.visibility =  View.VISIBLE
            mBinding?.errorText?.text = value
        }
    override var currentPasswordVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.currentPasswordVisibility = if(value) View.VISIBLE else View.GONE
        }
    override var usernameVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.usernameVisibility = if(value) View.VISIBLE else View.GONE
        }


    override var fieldsEnabled: Boolean = true
        set(value){
            super.fieldsEnabled = value
            field = value
        }

    override var entity: PersonWithAccount? = null
        set(value) {
            field = value
            mBinding?.person = value
            ustadFragmentTitle = value?.fullName()
        }

    override val mEditPresenter: UstadEditPresenter<*, PersonWithAccount>?
        get() = mPresenter

    private val uiState = PersonAccountEditUiState()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        mBinding = FragmentPersonAccountEditBinding.inflate(inflater, container,
            false).also {
        }

        mPresenter = PersonAccountEditPresenter(requireContext(), arguments.toStringMap(),
            this, di, viewLifecycleOwner).withViewLifecycle()

        mBinding?.currentPasswordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.currentPasswordError = null
        })

        mBinding?.newPasswordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.passwordError = null
            mBinding?.passwordConfirmError = null
        })


        mBinding?.confirmPasswordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.passwordConfirmError = null
            mBinding?.passwordError = null
        })

        mBinding?.accountUsernameText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.usernameError = null
        })

        mBinding?.accountUsernameText?.filters = arrayOf(USERNAME_FILTER)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    PersonAccountEditScreen(uiState)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(mBinding?.person != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                    mBinding?.person?.firstNames + " " + mBinding?.person?.lastName
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mBinding = null
        mPresenter = null
        entity = null
    }

    companion object{

        val USERNAME_FILTER = InputFilter { source, start, end, _, _, _ ->

            val sb = StringBuilder()

            var changed = false
            for (i in start until end) {
                val c = source[i]
                if(!BLOCK_CHARACTER_SET.contains(c)) {
                    sb.append(c.toString().lowercase())
                }else {
                    changed = true
                }
            }

            if(!changed) {
                null
            } else if(source is Spanned) {
                SpannableString(sb).also {
                    TextUtils.copySpansFrom(source, start, end, null, it, 0)
                }
            }else {
                sb
            }
        }
    }
}

@Composable
private fun PersonAccountEditScreen(
    uiState: PersonAccountEditUiState = PersonAccountEditUiState(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        if (uiState.usernameVisible){
            UstadTextEditField(
                value = uiState.personUsernameAndPassword.username,
                label = stringResource(id = R.string.username),
                onValueChange = {
                    uiState.personUsernameAndPassword.copy(
                        username = it
                    )
                },
                error = uiState.usernameError,
                enabled = uiState.fieldsEnabled,
            )
        }

        if (uiState.currentPasswordVisible){
            UstadTextEditField(
                value = uiState.personUsernameAndPassword.currentPassword,
                label = stringResource(id = R.string.current_password),
                onValueChange = {
                    uiState.personUsernameAndPassword.copy(
                        currentPassword = it
                    )
                },
                error = uiState.currentPasswordError,
                enabled = uiState.fieldsEnabled,
                password = true
            )
        }

        UstadTextEditField(
            value = uiState.personUsernameAndPassword.newPassword,
            label = stringResource(id = R.string.new_password),
            onValueChange = {
                uiState.personUsernameAndPassword.copy(
                    newPassword = it
                )
            },
            error = uiState.newPasswordError,
            enabled = uiState.fieldsEnabled,
            password = true
        )

        UstadTextEditField(
            value = uiState.personUsernameAndPassword.passwordConfirmed,
            label = stringResource(id = R.string.confirm_password),
            onValueChange = {
                uiState.personUsernameAndPassword.copy(
                    passwordConfirmed = it
                )
            },
            error = uiState.passwordConfirmedError,
            enabled = uiState.fieldsEnabled,
            password = true
        )
    }
}

@Composable
@Preview
fun PersonAccountEditScreenPreview() {
    MdcTheme {
        PersonAccountEditScreen()
    }
}