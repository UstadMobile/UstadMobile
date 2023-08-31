package com.ustadmobile.libuicompose.view.siteenterlink

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkUiState
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import com.ustadmobile.libuicompose.components.UstadErrorText
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SiteEnterLinkScreenForViewModel(
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

        Text(stringResource(MR.strings.please_enter_the_linK))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("site_link_text"),
            value = uiState.siteLink,
            label = { Text(stringResource(MR.strings.site_link)) },
            onValueChange = {
                onEditTextValueChange(it)
            },
            isError = uiState.linkError != null,
            enabled = uiState.fieldsEnabled,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(
                onGo = {
                    onClickNext()
                }
            )
        )

        uiState.linkError?.also {
            UstadErrorText(error = it)
        }


        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClickNext,
            enabled = uiState.fieldsEnabled,
            modifier = Modifier
                .testTag("next_button")
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondary
            )
        ) {
            Text(stringResource(MR.strings.next).uppercase(),
                color = contentColorFor(MaterialTheme.colors.secondary)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(stringResource(MR.strings.or).uppercase())

        Button(
            onClick = onClickNewLearningEnvironment,
            modifier = Modifier
                .testTag("create_new_button")
                .fillMaxWidth(),
            elevation = null,
            enabled = uiState.fieldsEnabled,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colors.primary,
            )
        ) {

            Icon(
                Icons.Filled.Add,
                contentDescription = "",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )

            Spacer(Modifier.size(ButtonDefaults.IconSpacing))

            Text(stringResource(MR.strings.create_a_new_learning_env)
                .uppercase()
            )
        }
    }
}