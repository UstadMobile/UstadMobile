package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI


data class ScheduleEditUiState(

    val entity: Schedule? = null,

    val fromTimeError: String? = null,

    val toTimeError: String? = null,

    val fieldsEnabled: Boolean = true,

)


class ScheduleEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadEditViewModel(di, savedStateHandle, ScheduleEditView.VIEW_NAME) {

    private val _uiState = MutableStateFlow(ScheduleEditUiState(fieldsEnabled = false))

    val uiState: Flow<ScheduleEditUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = createEditTitle(MessageID.add_a_schedule, MessageID.edit_schedule),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.done),
                    onClick = this::onClickSave
                )
            )
        }

        viewModelScope.launch {
            loadEntity<Schedule>(
                makeDefault = {
                    Schedule().apply {
                        scheduleUid = activeDb.doorPrimaryKeyManager.nextIdAsync(Schedule.TABLE_ID)
                        scheduleActive = true
                        scheduleFrequency = Schedule.SCHEDULE_FREQUENCY_WEEKLY
                    }
                },
                onLoadFromDb = { null }, //Does not load from database
                uiUpdate = { schedule ->
                    _uiState.update { prev ->
                        prev.copy(entity = schedule)
                    }
                }
            )

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }
        }
    }

    fun onEntityChanged(entity: Schedule?) {
        _uiState.update { prev ->
            prev.copy(
                entity = entity,
                toTimeError = updateErrorMessageOnChange(prev.entity?.sceduleStartTime,
                    entity?.sceduleStartTime, prev.toTimeError),
                fromTimeError = updateErrorMessageOnChange(prev.entity?.scheduleEndTime,
                    entity?.scheduleEndTime, prev.fromTimeError)
            )
        }

        scheduleEntityCommitToSavedState(
            entity = entity,
            serializer = Schedule.serializer(),
            commitDelay = 200
        )
    }

    fun onClickSave() {
        val schedule = _uiState.value.entity ?: return

        _uiState.update { prev ->
            prev.copy(
                fromTimeError =  if(schedule.sceduleStartTime == 0L) {
                    systemImpl.getString(MessageID.field_required_prompt)
                }else {
                    null
                },
                toTimeError = if(schedule.scheduleEndTime == 0L) {
                    systemImpl.getString(MessageID.field_required_prompt)
                }else if(schedule.scheduleEndTime <= schedule.sceduleStartTime) {
                    systemImpl.getString(MessageID.end_is_before_start_error)
                }else {
                    null
                }
            )
        }

        if(_uiState.value.fromTimeError == null && _uiState.value.toTimeError == null) {
            finishWithResult(schedule)
        }
    }



}
