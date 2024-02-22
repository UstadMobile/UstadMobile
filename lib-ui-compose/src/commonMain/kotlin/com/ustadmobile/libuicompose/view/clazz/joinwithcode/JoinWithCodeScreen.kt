package com.ustadmobile.libuicompose.view.clazz.joinwithcode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.viewmodel.clazz.joinwithcode.JoinWithCodeUiState
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding

import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource

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

        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
            errorText = uiState.codeError,
        ) {
            OutlinedTextField(
                modifier = Modifier.defaultItemPadding().testTag("code_text")
                    .fillMaxWidth(),
                value = uiState.code,
                label = {
                    Text(stringResource(MR.strings.entity_code, uiState.entityType))
                },
                isError = uiState.codeError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onCodeValueChange(it)
                },
            )
        }

        Button(
            onClick = onClickDone,
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("join_button"),
            enabled = uiState.fieldsEnabled,
        ) {
            Text(uiState.buttonLabel)
        }
    }
}
