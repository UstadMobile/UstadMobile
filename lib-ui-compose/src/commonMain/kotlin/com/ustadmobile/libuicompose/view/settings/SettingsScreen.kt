package com.ustadmobile.libuicompose.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.settings.SettingsUiState
import com.ustadmobile.libuicompose.components.UstadDetailField2
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.settings.SettingsViewModel
import com.ustadmobile.libuicompose.components.UstadDetailHeader
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.components.UstadWaitForRestartDialog
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import javax.swing.Icon

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(SettingsUiState())

    SettingsScreen(
        uiState = uiState,
        onClickAppLanguage = viewModel::onClickLanguage,
        onClickWorkspace = viewModel::onClickSiteSettings,
        onClickHtmlContentDisplayEngine = viewModel::onClickHtmlContentDisplayEngine,
        onClickVersion = viewModel::onClickVersion,
        onClickDeveloperOptions = viewModel::onClickDeveloperOptions,
    )

    if(uiState.langDialogVisible) {
        //As per https://developer.android.com/jetpack/compose/components/dialog
        SettingsDialog(
            onDismissRequest = viewModel::onDismissLangDialog,
        ) {
            uiState.availableLanguages.forEach { lang ->
                ListItem(
                    modifier = Modifier.clickable { viewModel.onClickLang(lang) },
                    headlineContent = { Text(lang.langDisplay) }
                )
            }
        }
    }

    if(uiState.htmlContentDisplayDialogVisible) {
        SettingsDialog(
            onDismissRequest = viewModel::onDismissHtmlContentDisplayEngineDialog,
        ) {
            uiState.htmlContentDisplayOptions.forEach { engineOption ->
                ListItem(
                    modifier = Modifier.clickable {
                        viewModel.onClickHtmlContentDisplayEngineOption(engineOption)
                    },
                    headlineContent = { Text(stringResource(engineOption.stringResource)) },
                    supportingContent = engineOption.explanationStringResource?.let {
                        { Text(stringResource(it)) }
                    }
                )
            }
        }
    }

    if(uiState.waitForRestartDialogVisible) {
        UstadWaitForRestartDialog()
    }

}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onClickAppLanguage: () -> Unit = {},
    onClickHtmlContentDisplayEngine: () -> Unit = {},
    onClickGoToHolidayCalendarList: () -> Unit = {},
    onClickWorkspace: () -> Unit = {},
    onClickLeavingReason: () -> Unit = {},
    onClickVersion: () -> Unit = { },
    onClickDeveloperOptions: () -> Unit = { },
) {
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize()
    )  {

        UstadDetailField2(
            modifier = Modifier.clickable { onClickAppLanguage() },
            icon= Icons.Default.Language,
            valueText = uiState.currentLanguage,
            labelText = stringResource(MR.strings.app_language),
        )

        if (uiState.holidayCalendarVisible){
            UstadDetailField2(
                valueText = stringResource(MR.strings.holiday_calendars),
                labelText = stringResource(MR.strings.holiday_calendars_desc),
                modifier = Modifier.clickable { onClickGoToHolidayCalendarList() },
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.workspaceSettingsVisible){
            UstadDetailField2(
                icon = Icons.Default.Workspaces,
                valueText = stringResource(MR.strings.site),
                labelText = stringResource(MR.strings.manage_site_settings),
                modifier = Modifier.clickable { onClickWorkspace() },
            )
        }


        if (uiState.reasonLeavingVisible){
            UstadDetailField2(
                icon = Icons.Default.Logout,
                valueText = stringResource(MR.strings.leaving_reason),
                labelText = stringResource(MR.strings.leaving_reason_manage),
                modifier = Modifier.clickable { onClickLeavingReason() },
            )
        }

        if(uiState.advancedSectionVisible) {
            UstadDetailHeader { Text(stringResource(MR.strings.advanced)) }

            if(uiState.htmlContentDisplayEngineVisible) {
                UstadDetailField2(
                    modifier = Modifier.clickable { onClickHtmlContentDisplayEngine() },
                    icon = Icons.Default.DisplaySettings,
                    valueText = uiState.currentHtmlContentDisplayOption?.stringResource
                        ?.let { stringResource(it) } ?: "",
                    labelText = stringResource(MR.strings.html5_content_display_engine),
                )
            }
        }

        if(uiState.showDeveloperOptions) {
            //Developer settings are not translated
            UstadDetailField2(
                modifier = Modifier.clickable { onClickDeveloperOptions() },
                icon = Icons.Default.DeveloperMode,
                valueText = "Developer Settings",
                labelText = "File paths, logging options, etc.",
            )
        }

        Divider(modifier = Modifier.height(1.dp))

        ListItem(
            modifier = Modifier.testTag("settings_version").clickable { onClickVersion() },
            headlineContent = { Text(uiState.version) },
            supportingContent = { Text(stringResource(MR.strings.version)) }
        )
    }
}