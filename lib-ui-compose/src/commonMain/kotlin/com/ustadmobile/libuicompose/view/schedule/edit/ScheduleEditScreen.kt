package com.ustadmobile.libuicompose.view.schedule.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.components.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadTimeField
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding

@Composable
fun ScheduleEditScreen(
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
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {
        UstadMessageIdOptionExposedDropDownMenuField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding(),
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

        Row(
            modifier = Modifier.fillMaxWidth().defaultItemPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
        ){
            UstadInputFieldLayout(
                modifier = Modifier.weight(0.5f).padding(end = 8.dp),
                errorText = uiState.fromTimeError
            ) {
                UstadTimeField(
                    value = (uiState.entity?.sceduleStartTime ?: 0).toInt(),
                    label = { Text(stringResource(MR.strings.from)) },
                    isError = uiState.fromTimeError != null,
                    enabled = uiState.fieldsEnabled,
                    onValueChange = {
                        onScheduleChanged(uiState.entity?.shallowCopy{
                            sceduleStartTime = it.toLong()
                        })
                    }
                )
            }

            UstadInputFieldLayout(
                modifier = Modifier.weight(0.5f).padding(start = 8.dp),
                errorText = uiState.toTimeError
            ) {
                UstadTimeField(
                    value = (uiState.entity?.scheduleEndTime ?: 0).toInt(),
                    label = { Text(stringResource(MR.strings.to_key)) },
                    isError = uiState.toTimeError != null,
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
}
