package com.ustadmobile.libuicompose.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.SettingsUiState
import com.ustadmobile.libuicompose.components.UstadDetailField2
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@Composable
fun SettingsScreen(
    uiState: SettingsUiState = SettingsUiState(),
    onClickAppLanguage: () -> Unit = {},
    onClickGoToHolidayCalendarList: () -> Unit = {},
    onClickWorkspace: () -> Unit = {},
    onClickLeavingReason: () -> Unit = {},
    onClickLangList: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    )  {

        UstadDetailField2(
            modifier = Modifier.clickable { onClickAppLanguage() },
            icon= Icons.Default.Language,
            valueText = uiState.currentLanguage,
            labelText = stringResource(MR.strings.app_language),
        )

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.reasonLeavingVisible){
            UstadDetailField2(
                icon = Icons.Default.Logout,
                valueText = stringResource(MR.strings.leaving_reason),
                labelText = stringResource(MR.strings.leaving_reason_manage),
                modifier = Modifier.clickable { onClickLeavingReason() },
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.langListVisible){
            UstadDetailField2(
                icon = Icons.Default.Language,
                valueText = stringResource(MR.strings.languages),
                labelText = stringResource(MR.strings.languages_description),
                modifier = Modifier.clickable { onClickLangList() },
            )
        }

    }
}