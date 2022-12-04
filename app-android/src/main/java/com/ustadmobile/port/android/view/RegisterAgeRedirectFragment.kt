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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentRegisterAgeRedirectBinding
import com.ustadmobile.core.controller.RegisterAgeRedirectPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.RegisterAgeRedirectView
import com.ustadmobile.core.viewmodel.RegisterAgeRedirectUiState
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.composable.DatePickerSpinner

class RegisterAgeRedirectFragment() : UstadBaseFragment(), RegisterAgeRedirectView {

    private var mBinding: FragmentRegisterAgeRedirectBinding? = null

    private var mPresenter: RegisterAgeRedirectPresenter? = null

    override var dateOfBirth: Long
        get() = mBinding?.dateOfBirth ?: 0
        set(value) {
            mBinding?.dateOfBirth = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentRegisterAgeRedirectBinding.inflate(inflater, container, false).also {
            it.datePicker.maxDate = systemTimeInMillis()
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    RegisterAgeRedirectScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = RegisterAgeRedirectPresenter(requireContext(), arguments.toStringMap(),
            this, di).withViewLifecycle()
        mBinding?.presenter = mPresenter
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        mBinding = null
    }
}

@Composable
private fun RegisterAgeRedirectScreen(
    uiState: RegisterAgeRedirectUiState = RegisterAgeRedirectUiState(),
    onSetDate: (Long) -> Unit = {},
    onClickNext: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        Text(text = stringResource(id = R.string.what_is_your_date_of_birth))

        Spacer(modifier = Modifier.height(10.dp))

        DatePickerSpinner(
            date = uiState.dateOfBirth,
            onSetDate = onSetDate
        )

        Button(
            onClick = onClickNext,
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.secondaryColor)
            )
        ) {
            Text(
                stringResource(R.string.next).uppercase(),
                color = contentColorFor(
                    colorResource(id = R.color.secondaryColor)
                )
            )
        }
    }
}

@Composable
@Preview
fun RegisterAgeRedirectScreenPreview() {
    MdcTheme {
        RegisterAgeRedirectScreen()
    }
}