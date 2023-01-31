package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.SiteEnterLinkUiState
import com.ustadmobile.core.viewmodel.SiteEnterLinkViewModel
import com.ustadmobile.port.android.view.composable.UstadTextEditField

class SiteEnterLinkFragment : UstadBaseMvvmFragment() {

    private val viewModel: SiteEnterLinkViewModel by viewModels {
        UstadViewModelProviderFactory(di, this, requireArguments())
    }

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

@Composable
private fun SiteEnterLinkScreen(
    uiState: SiteEnterLinkUiState = SiteEnterLinkUiState(),
    onClickNext: () -> Unit = {},
    onClickNewLearningEnvironment: () -> Unit = {},
    onEditTextValueChange: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        Image(
            painter = painterResource(id = R.drawable.illustration_connect),
            contentDescription = null,
            modifier = Modifier
                .height(200.dp))

        Text(stringResource(R.string.please_enter_the_linK))

        UstadTextEditField(
            value = uiState.siteLink,
            label = stringResource(id = R.string.site_link),
            onValueChange = onEditTextValueChange,
            errorString = uiState.linkError,
            enabled = uiState.fieldsEnabled,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(
                onGo = {
                    onClickNext()
                }
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClickNext,
            enabled = uiState.fieldsEnabled,
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.secondaryColor)
            )
        ) {
            Text(stringResource(R.string.next).uppercase(),
                color = contentColorFor(
                    colorResource(id = R.color.secondaryColor)
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(stringResource(R.string.or).uppercase())

        Button(
            onClick = onClickNewLearningEnvironment,
            modifier = Modifier
                .fillMaxWidth(),
            elevation = null,
            enabled = uiState.fieldsEnabled,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent,
                contentColor = colorResource(id = R.color.primaryColor),
            )
        ) {

            Icon(Icons.Filled.Add,
                contentDescription = "",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )

            Spacer(Modifier.size(ButtonDefaults.IconSpacing))

            Text(stringResource(R.string.create_a_new_learning_env)
                .uppercase()
            )
        }
    }
}

@Composable
private fun SiteEnterLinkScreenForViewModel(
    viewModel: SiteEnterLinkViewModel
) {
    val uiState: SiteEnterLinkUiState by viewModel.uiState.collectAsState(
        initial = SiteEnterLinkUiState()
    )

    SiteEnterLinkScreen(
        uiState = uiState,
        onClickNext = viewModel::onClickNext,
        onClickNewLearningEnvironment = { },
        onEditTextValueChange = viewModel::onSiteLinkUpdated,
    )
}

@Composable
@Preview
fun SiteEnterLinkScreenPreview() {
    MdcTheme {
        SiteEnterLinkScreen()
    }
}

