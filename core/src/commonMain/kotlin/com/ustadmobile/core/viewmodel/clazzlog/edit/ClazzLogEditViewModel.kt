package com.ustadmobile.core.viewmodel.clazzlog.edit

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.schedule.generateUid
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ClazzLogEditUiState(

    val fieldsEnabled: Boolean = true,

    val clazzLog: ClazzLog? = null,

    val timeZone: String = "UTC",

    val dateError: String? = null,

)

class ClazzLogEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val timeZone = savedStateHandle[ARG_TIME_ZONE] ?: "UTC"

    private val _uiState = MutableStateFlow(ClazzLogEditUiState(timeZone = timeZone))

    val uiState: Flow<ClazzLogEditUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MessageID.add_a_new_occurrence),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.next),
                    onClick = this::onClickSave
                )
            )
        }

        viewModelScope.launch {
            loadEntity(
                serializer = ClazzLog.serializer(),
                onLoadFromDb = {
                    null
                },
                makeDefault = {
                    ClazzLog().apply {
                        clazzLogClazzUid = savedStateHandle[UstadView.ARG_CLAZZUID]?.toLong() ?: 0
                        logDate = systemTimeInMillis()
                    }
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(clazzLog = it)
                    }
                }
            )
        }
    }

    fun onEntityChanged(clazzLog: ClazzLog?) {
        _uiState.update { prev ->
            prev.copy(
                clazzLog = clazzLog,
                dateError = updateErrorMessageOnChange(
                    prevFieldValue = prev.clazzLog?.logDate,
                    currentFieldValue = clazzLog?.logDate,
                    currentErrorMessage = prev.dateError
                )
            )
        }

        scheduleEntityCommitToSavedState(
            entity = clazzLog,
            serializer = ClazzLog.serializer(),
            commitDelay = 200,
        )
    }

    fun onClickSave() {
        val clazzLog = _uiState.value.clazzLog ?: return

        if(!clazzLog.logDate.isDateSet()) {
            _uiState.update { prev ->
                prev.copy(
                    dateError = systemImpl.getString(MessageID.field_required_prompt)
                )
            }

            return
        }

        val clazzLogWithUid = clazzLog.shallowCopy {
            clazzLogUid = generateUid()
        }

        val newClazzLogJson = json.encodeToString(ClazzLog.serializer(), clazzLogWithUid)
        navController.navigate(
            viewName = ClazzLogEditAttendanceViewModel.DEST_NAME,
            args = mapOf(
                ClazzLogEditAttendanceView.ARG_NEW_CLAZZLOG to newClazzLogJson,
            )
        )
    }

    companion object {

        const val DEST_NAME = "ClazzLogEdit"

    }

}
