package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI


data class InviteStudentsUiState(

    val fieldsEnabled: Boolean = true,

    val studentsList: List<Person> = emptyList(),

)

class InviteStudentsViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(InviteStudentsUiState(fieldsEnabled = false))

    val uiState: Flow<InviteStudentsUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = createEditTitle(MessageID.invit, MessageID.edit_schedule),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.done),
                    onClick = this::onClickSave
                )
            )
        }

        viewModelScope.launch {
            loadEntity<Schedule>(
                serializer = Schedule.serializer(),
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

    companion object {

        const val DEST_NAME = "InviteStudents"

    }

}
