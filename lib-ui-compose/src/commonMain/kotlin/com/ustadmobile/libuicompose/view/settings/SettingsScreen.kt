@file:OptIn(ExperimentalMaterial3Api::class)

package com.ustadmobile.libuicompose.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.viewmodel.settings.SettingsUiState
import com.ustadmobile.core.viewmodel.settings.SettingsViewModel
import com.ustadmobile.libuicompose.components.UstadBottomSheetOption
import com.ustadmobile.libuicompose.components.UstadDetailField2
import com.ustadmobile.libuicompose.components.UstadDetailHeader
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.components.UstadWaitForRestartDialog
import dev.icerock.moko.resources.compose.stringResource
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(SettingsUiState())
    var shareAppOptionsVisible by remember { mutableStateOf(false) }

    SettingsScreen(uiState = uiState,
        onClickAppLanguage = viewModel::onClickLanguage,
        onClickWorkspace = viewModel::onClickSiteSettings,
        onClickHtmlContentDisplayEngine = viewModel::onClickHtmlContentDisplayEngine,
        onClickVersion = viewModel::onClickVersion,
        onClickDeveloperOptions = viewModel::onClickDeveloperOptions,
        onClickDeletedItems = viewModel::onClickDeletedItems,
        onClickOfflineStorageOptionsDialog = viewModel::onClickOfflineStorageOptionsDialog,
        onClickAppShare = { shareAppOptionsVisible = true })

    if (uiState.langDialogVisible) {
        // As per https://developer.android.com/jetpack/compose/components/dialog
        SettingsDialog(
            onDismissRequest = viewModel::onDismissLangDialog,
        ) {
            uiState.availableLanguages.forEach { lang ->
                ListItem(modifier = Modifier.clickable { viewModel.onClickLang(lang) },
                    headlineContent = { Text(lang.langDisplay) })
            }
        }
    }

    if (uiState.htmlContentDisplayDialogVisible) {
        SettingsDialog(
            onDismissRequest = viewModel::onDismissHtmlContentDisplayEngineDialog,
        ) {
            uiState.htmlContentDisplayOptions.forEach { engineOption ->
                ListItem(modifier = Modifier.clickable {
                    viewModel.onClickHtmlContentDisplayEngineOption(engineOption)
                },
                    headlineContent = { Text(stringResource(engineOption.stringResource)) },
                    supportingContent = engineOption.explanationStringResource?.let {
                        { Text(stringResource(it)) }
                    })
            }
        }
    }

    if (uiState.storageOptionsDialogVisible) {
        SettingsDialog(
            onDismissRequest = viewModel::onDismissOfflineStorageOptionsDialog
        ) {
            uiState.storageOptions.forEach { option ->
                ListItem(modifier = Modifier.clickable {
                    viewModel.onSelectOfflineStorageOption(option.option)
                },
                    headlineContent = { Text(stringResource(option.option.label)) },
                    supportingContent = {
                        Text(
                            stringResource(
                                MR.strings.space_available,
                                UMFileUtil.formatFileSize(option.availableSpace)
                            )
                        )
                    })
            }
        }
    }

    if (uiState.waitForRestartDialogVisible) {
        UstadWaitForRestartDialog()
    }

    if (shareAppOptionsVisible) {
        ModalBottomSheet(onDismissRequest = { shareAppOptionsVisible = false }) {
            UstadBottomSheetOption(
                modifier = Modifier.clickable {
                    shareAppOptionsVisible = false
                    viewModel.onClickAppShare(false)
                },
                headlineContent = { Text(stringResource(MR.strings.send_apk_file)) },
                leadingContent = { Icon(Icons.Outlined.Android, contentDescription = null) },


                )

            UstadBottomSheetOption(
                modifier = Modifier.clickable {
                    shareAppOptionsVisible = false
                    viewModel.onClickAppShare(true)

                },
                headlineContent = { Text(stringResource(MR.strings.send_app_link)) },
                leadingContent = {
                Icon(
                        Icons.Outlined.Link, contentDescription = null
                    )
                },

                )
        }
    }
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onClickAppShare: () -> Unit = {},
    onClickAppLanguage: () -> Unit = {},
    onClickHtmlContentDisplayEngine: () -> Unit = {},
    onClickGoToHolidayCalendarList: () -> Unit = {},
    onClickWorkspace: () -> Unit = {},
    onClickLeavingReason: () -> Unit = {},
    onClickVersion: () -> Unit = {},
    onClickDeveloperOptions: () -> Unit = {},
    onClickDeletedItems: () -> Unit = {},
    onClickOfflineStorageOptionsDialog: () -> Unit = {},
) {
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        UstadDetailField2(
            modifier = Modifier.clickable { onClickAppLanguage() },
            icon = Icons.Default.Language,
            valueText = uiState.currentLanguage,
            labelText = stringResource(MR.strings.app_language),
        )

        if (uiState.storageOptionsVisible) {
            UstadDetailField2(
                modifier = Modifier.clickable {
                    onClickOfflineStorageOptionsDialog()
                },
                labelText = stringResource(MR.strings.offline_items_storage),
                valueText = uiState.selectedOfflineStorageOption?.label?.let {
                    stringResource(it)
                } ?: "",
                icon = Icons.Default.SdStorage,
            )
        }

        UstadDetailField2(valueText = stringResource(MR.strings.deleted_items),
            labelText = stringResource(MR.strings.delete_or_restore_items),
            icon = Icons.Default.Delete,
            modifier = Modifier.clickable { onClickDeletedItems() })

        if (uiState.holidayCalendarVisible) {
            UstadDetailField2(
                valueText = stringResource(MR.strings.holiday_calendars),
                labelText = stringResource(MR.strings.holiday_calendars_desc),
                modifier = Modifier.clickable { onClickGoToHolidayCalendarList() },
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.workspaceSettingsVisible) {
            UstadDetailField2(
                icon = Icons.Default.Workspaces,
                valueText = stringResource(MR.strings.site),
                labelText = stringResource(MR.strings.manage_site_settings),
                modifier = Modifier.clickable { onClickWorkspace() },
            )
        }

        if (uiState.sendAppOptionVisible) {
            UstadDetailField2(
                modifier = Modifier.clickable {
                    onClickAppShare()
                },
                valueText = stringResource(MR.strings.share_app_title),
                labelText = stringResource(MR.strings.share_app_description),
                icon = Icons.Default.Share,
            )
        }

        if (uiState.reasonLeavingVisible) {
            UstadDetailField2(
                icon = Icons.AutoMirrored.Filled.Logout,
                valueText = stringResource(MR.strings.leaving_reason),
                labelText = stringResource(MR.strings.leaving_reason_manage),
                modifier = Modifier.clickable { onClickLeavingReason() },
            )
        }

        if (uiState.advancedSectionVisible) {
            UstadDetailHeader { Text(stringResource(MR.strings.advanced)) }

            if (uiState.htmlContentDisplayEngineVisible) {
                UstadDetailField2(
                    modifier = Modifier.clickable { onClickHtmlContentDisplayEngine() },
                    icon = Icons.Default.DisplaySettings,
                    valueText = uiState.currentHtmlContentDisplayOption?.stringResource?.let {
                            stringResource(it)
                        } ?: "",
                    labelText = stringResource(MR.strings.html5_content_display_engine),
                )
            }
        }

        if (uiState.showDeveloperOptions) {
            // Developer settings are not translated
            UstadDetailField2(
                modifier = Modifier.clickable { onClickDeveloperOptions() },
                icon = Icons.Default.DeveloperMode,
                valueText = "Developer Settings",
                labelText = "File paths, logging options, etc.",
            )
        }

        HorizontalDivider(thickness = 1.dp)

        ListItem(modifier = Modifier.testTag("settings_version").clickable { onClickVersion() },
            headlineContent = { Text(uiState.version) },
            supportingContent = { Text(stringResource(MR.strings.version)) })
    }
}
