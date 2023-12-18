package com.ustadmobile.libuicompose.view.contententry.importlink

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.contententry.importlink.ContentEntryImportLinkUiState
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@Composable
fun ContentEntryImportLinkScreen(
    uiState: ContentEntryImportLinkUiState,
    onClickNext: () -> Unit = {},
    onUrlChange: (String?) -> Unit = {}
){
    Column (
        modifier = Modifier
            .padding(vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        UstadInputFieldLayout(
            errorText = uiState.linkError,
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                ,
                value = uiState.url,
                label = {
                    Text(stringResource(MR.strings.enter_url))
                },
                isError = uiState.linkError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onUrlChange(it)
                }
            )
        }


        Text(
            text = stringResource(MR.strings.supported_link),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Button(
            onClick = onClickNext,
            enabled = uiState.fieldsEnabled,
            modifier = Modifier
                .padding(vertical = 26.dp, horizontal = 8.dp)
                .fillMaxWidth(),
        ) {
            Text(
                stringResource(MR.strings.next)
            )
        }

    }
}
