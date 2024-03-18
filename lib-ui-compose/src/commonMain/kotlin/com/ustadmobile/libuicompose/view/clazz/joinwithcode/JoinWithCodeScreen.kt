package com.ustadmobile.libuicompose.view.clazz.joinwithcode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import com.ustadmobile.core.viewmodel.clazz.joinwithcode.JoinWithCodeUiState
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.clazz.joinwithcode.JoinWithCodeViewModel
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers

@Composable
fun JoinWithCodeScreen(
    viewModel: JoinWithCodeViewModel
) {
    val uiStateVal by viewModel.uiState.collectAsState(
        JoinWithCodeUiState(), Dispatchers.Main.immediate
    )

    JoinWithCodeScreen(
        uiState = uiStateVal,
        onCodeValueChange = viewModel::onCodeValueChange,
        onClickDone = viewModel::onClickJoin,
    )
}


@Composable
fun JoinWithCodeScreen(
    uiState: JoinWithCodeUiState,
    onCodeValueChange: (String) -> Unit = {},
    onClickDone: () -> Unit = {},
){
    Column (
        modifier = Modifier
            .defaultScreenPadding()
            .fillMaxSize(),
    ){

       Text(
           stringResource(MR.strings.join_code_instructions),
           modifier = Modifier.defaultItemPadding()
       )

        OutlinedTextField(
            modifier = Modifier.defaultItemPadding().testTag("invite_code")
                .fillMaxWidth(),
            value = uiState.code,
            label = {
                Text(stringResource(MR.strings.invite_code) + "*")
            },
            isError = uiState.codeError != null,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onCodeValueChange(it)
            },
            supportingText = {
                Text(uiState.codeError ?: stringResource(MR.strings.required))
            },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onClickDone() }
            ),
        )

        Button(
            onClick = onClickDone,
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("submit_button"),
            enabled = uiState.fieldsEnabled,
        ) {
            Text(stringResource(MR.strings.submit))
        }
    }
}
