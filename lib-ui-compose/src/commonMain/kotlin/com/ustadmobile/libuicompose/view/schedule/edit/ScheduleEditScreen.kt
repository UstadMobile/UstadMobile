package com.ustadmobile.libuicompose.view.schedule.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.core.viewmodel.schedule.edit.ScheduleEditUiState
import com.ustadmobile.core.viewmodel.schedule.edit.ScheduleEditViewModel
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@Composable
fun ScheduleEditScreenForViewModel(
    viewModel: ScheduleEditViewModel
){
    val uiState: ScheduleEditUiState by viewModel.uiState.collectAsState(initial = ScheduleEditUiState())

    ScheduleEditScreen(
        uiState = uiState,
        onScheduleChanged = viewModel::onEntityChanged
    )
}

@Composable
fun ScheduleEditScreen(
    uiState: ScheduleEditUiState = ScheduleEditUiState(),
    onScheduleChanged: (Schedule?) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        UstadMessageIdOptionExposedDropDownMenuField(
            value = uiState.entity?.scheduleDay ?: 0,
            label = stringResource(MR.strings.day),
            options = ScheduleConstants.DAY_MESSAGE_IDS,
            onOptionSelected = {
                onScheduleChanged(uiState.entity?.shallowCopy{
                    scheduleDay = it.value
                })
            },
            enabled = uiState.fieldsEnabled,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row {

            UstadTimeEditTextField(
                modifier = Modifier.weight(0.5F),
                value = (uiState.entity?.sceduleStartTime ?: 0).toInt(),
                label = stringResource(MR.strings.from),
                error = uiState.fromTimeError,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onScheduleChanged(uiState.entity?.shallowCopy{
                        sceduleStartTime = it.toLong()
                    })
                }
            )

            Spacer(modifier = Modifier.width(10.dp))

            UstadTimeEditTextField(
                modifier = Modifier.weight(0.5F),
                value = (uiState.entity?.scheduleEndTime ?: 0).toInt(),
                label = stringResource(MR.strings.to_key),
                error = uiState.toTimeError,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onScheduleChanged(uiState.entity?.shallowCopy{
                        scheduleEndTime = it.toLong()
                    })
                }
            )
        }
    }
}
