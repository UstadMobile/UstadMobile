package com.ustadmobile.libuicompose.view.appearance

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.appearance.AppearanceDetailUiState
import com.ustadmobile.core.viewmodel.appearance.AppearanceDetailViewModel
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun AppearanceDetailScreen(viewModel: AppearanceDetailViewModel) {
    val uiState: AppearanceDetailUiState by viewModel.uiState.collectAsStateWithLifecycle(
        AppearanceDetailUiState(), Dispatchers.Main.immediate
    )

    AppearanceDetailScreen(
        uiState = uiState,
    )
}

@Composable
fun AppearanceDetailScreen(
    uiState: AppearanceDetailUiState = AppearanceDetailUiState(),
) {
    UstadVerticalScrollColumn(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        Image(
            imageVector = Icons.Default.AccountCircle,
            contentDescription =  stringResource(MR.strings.organisation_logo),
            modifier = Modifier.size(60.dp)
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
            value = uiState.organisationName ?: "",
            onValueChange = { /* Read-only */ },
            label = { Text( stringResource(MR.strings.organisation_name),) },
            readOnly = true,
            singleLine = true
        )

        Text(
            text = stringResource(MR.strings.jetpack_compose_theme),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
            value = uiState.jetpackComposeTheme ?: stringResource(MR.strings.no_file_chosen),
            onValueChange = { /* Read-only */ },
            label = { Text(stringResource(MR.strings.jetpack_compose_theme)) },
            readOnly = true,
            singleLine = true
        )

        Text(
            text = stringResource(MR.strings.mui_theme),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )


        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
            value = uiState.muiTheme ?: stringResource(MR.strings.no_file_chosen),
            onValueChange = { /* Read-only */ },
            label = { Text(stringResource(MR.strings.mui_theme)) },
            readOnly = true,
            singleLine = true
        )
    }
}