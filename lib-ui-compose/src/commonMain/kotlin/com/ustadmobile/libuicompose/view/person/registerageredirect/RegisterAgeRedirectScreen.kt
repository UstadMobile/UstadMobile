package com.ustadmobile.libuicompose.view.person.registerageredirect

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectUiState
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun RegisterAgeRedirectScreen(
    viewModel: RegisterAgeRedirectViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        RegisterAgeRedirectUiState(), Dispatchers.Main.immediate)

    RegisterAgeRedirectScreen(
        uiState = uiState,
        onSetDate = viewModel::onSetDate,
        onClickNext = viewModel::onClickNext,
    )
}


@Composable
fun RegisterAgeRedirectScreen(
    uiState: RegisterAgeRedirectUiState = RegisterAgeRedirectUiState(),
    onSetDate: (Long) -> Unit = {},
    onClickNext: () -> Unit = {},
) {
    UstadVerticalScrollColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {


        Spacer(modifier = Modifier.height(8.dp))

        RegisterAgeRedirectDatePicker(
            date = uiState.dateOfBirth,
            onSetDate = onSetDate,
            supportingText = {
                Text(uiState.dateOfBirthError ?: stringResource(MR.strings.required))
            },
            isError = uiState.dateOfBirthError != null,
            maxDate = uiState.maxDate,
            onDone = onClickNext,
        )


    }
}
