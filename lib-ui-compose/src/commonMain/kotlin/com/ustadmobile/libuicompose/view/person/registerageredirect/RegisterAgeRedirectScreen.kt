package com.ustadmobile.libuicompose.view.person.registerageredirect

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.RegisterAgeRedirectUiState
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR


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

        Text(stringResource(MR.strings.what_is_your_date_of_birth))

        Spacer(modifier = Modifier.height(10.dp))

        RegisterAgeRedirectDatePicker(
            date = uiState.dateOfBirth,
            onSetDate = onSetDate
        )

        Button(
            onClick = onClickNext,
            modifier = Modifier.fillMaxWidth().testTag("next_button"),
        ) {
            Text(stringResource(MR.strings.next),)
        }
    }
}
