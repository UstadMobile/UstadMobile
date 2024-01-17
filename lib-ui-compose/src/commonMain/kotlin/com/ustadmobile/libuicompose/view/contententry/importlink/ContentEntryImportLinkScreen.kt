package com.ustadmobile.libuicompose.view.contententry.importlink

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.contententry.importlink.ContentEntryImportLinkUiState
import com.ustadmobile.core.viewmodel.contententry.importlink.ContentEntryImportLinkViewModel
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun ContentEntryImportLinkScreen(
    viewModel: ContentEntryImportLinkViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        ContentEntryImportLinkUiState(), Dispatchers.Main.immediate)
    ContentEntryImportLinkScreen(
        uiState = uiState,
        onUrlChange = viewModel::onChangeLink,
        onNext = viewModel::onClickNext,
    )
}

@Composable
fun ContentEntryImportLinkScreen(
    uiState: ContentEntryImportLinkUiState,
    onUrlChange: (String) -> Unit = {},
    onNext: () -> Unit = { },
){
    Column (
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        OutlinedTextField(
            modifier = Modifier
                .defaultItemPadding()
                .fillMaxWidth(),
            value = uiState.url,
            singleLine = true,
            label = {
                Text(stringResource(MR.strings.enter_url) + "*")
            },
            isError = uiState.linkError != null,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onUrlChange(it)
            },
            supportingText = {
                Text(uiState.linkError ?: stringResource(MR.strings.required))
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onNext() }
            )
        )

        Text(
            text = stringResource(MR.strings.supported_link),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.defaultItemPadding(),
        )
    }
}
