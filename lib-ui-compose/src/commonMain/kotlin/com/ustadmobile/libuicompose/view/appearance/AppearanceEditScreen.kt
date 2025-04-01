package com.ustadmobile.libuicompose.view.appearance

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.appearance.AppearanceEditUiState
import com.ustadmobile.core.viewmodel.appearance.AppearanceEditViewModel
import com.ustadmobile.libuicompose.components.UstadImageSelectButton
import com.ustadmobile.libuicompose.components.UstadPickFileOpts
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.components.rememberUstadFilePickLauncher
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun AppearanceEditScreen(viewModel: AppearanceEditViewModel) {
    val uiState: AppearanceEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        AppearanceEditUiState(), Dispatchers.Main.immediate
    )

    AppearanceEditScreen(
        uiState = uiState,
        onOrganisationNameChanged = viewModel::onOrganisationNameChanged,
        onOrganisationLogoChanged = viewModel::onOrganisationLogoChanged,
        onChooseJetpackComposeTheme = viewModel::onJetpackComposeThemeChanged,
        onChooseMuiTheme = viewModel::onMuiThemeChanged
    )
}

@Composable
fun AppearanceEditScreen(
    uiState: AppearanceEditUiState = AppearanceEditUiState(),
    onOrganisationNameChanged: (String) -> Unit = {},
    onOrganisationLogoChanged: (String?) -> Unit = {},
    onChooseJetpackComposeTheme: (String?) -> Unit = {},
    onChooseMuiTheme: (String?) -> Unit = {}
) {

    val jetpackComposeThemeLauncher = rememberUstadFilePickLauncher { result ->
        onChooseJetpackComposeTheme(result.uri)
    }

    val muiThemeLauncher = rememberUstadFilePickLauncher { result ->
        onChooseMuiTheme(result.uri)
    }


    UstadVerticalScrollColumn(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        UstadImageSelectButton(
            imageUri = uiState.organisationLogo,
            onImageUriChanged = onOrganisationLogoChanged,
            modifier = Modifier.height(20.dp),

        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
            value = uiState.organisationName ?: "",
            onValueChange = onOrganisationNameChanged,
            label = { Text( stringResource(MR.strings.organisation_name),) },
            singleLine = true,
            enabled = uiState.fieldsEnabled
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


        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { jetpackComposeThemeLauncher(UstadPickFileOpts()) },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(stringResource(MR.strings.choose_file))
                }
                Text(
                    text = uiState.jetpackComposeTheme?.let { uri ->
                        uri.substringAfterLast('/').takeIf { it.isNotBlank() }
                            ?: stringResource(MR.strings.no_file_chosen)
                    } ?: stringResource(MR.strings.no_file_chosen),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(MR.strings.mui_theme),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { muiThemeLauncher(UstadPickFileOpts()) },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(stringResource(MR.strings.choose_file))
                }
                Text(
                    text = uiState.muiTheme?.let { uri ->
                        uri.substringAfterLast('/').takeIf { it.isNotBlank() }
                            ?: stringResource(MR.strings.no_file_chosen)
                    } ?: stringResource(MR.strings.no_file_chosen),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
        }
    }
}