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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkUiState
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import com.ustadmobile.libuicompose.components.UstadErrorText
import com.ustadmobile.libuicompose.util.ext.onPreviewKeyEventFocusHandler
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun SiteEnterLinkScreen(
    viewModel: SiteEnterLinkViewModel
) {
    val uiState: SiteEnterLinkUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initial = SiteEnterLinkUiState(),  context = Dispatchers.Main.immediate
    )

    SiteEnterLinkScreen(
        uiState = uiState,
        onClickNext = viewModel::onClickNext,
        onClickNewLearningEnvironment = {},
        onEditTextValueChange = viewModel::onSiteLinkUpdated,
    )
}

@Composable
fun SiteEnterLinkScreen(
    uiState: SiteEnterLinkUiState,
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
        val focusManager = LocalFocusManager.current

        Image(
            painter = painterResource(
                imageResource = MR.images.illustration_connect
            ),
            contentDescription = null,
        )

        Text(stringResource(MR.strings.please_enter_the_linK))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("site_link_text")
                .onPreviewKeyEventFocusHandler(
                    focusManager, onEnter = {
                        onClickNext()
                        true
                    }
                ),
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
        ) {
            Text(stringResource(MR.strings.next).uppercase())
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(stringResource(MR.strings.or).uppercase())

        OutlinedButton(
            onClick = onClickNewLearningEnvironment ,
            modifier = Modifier
                .testTag("create_new_button")
                .fillMaxWidth(),
            elevation = null,
            enabled = uiState.fieldsEnabled,
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