package com.ustadmobile.core.viewmodel.respect.respectassignment.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.respect.respectassignment.detail.RespectAssignmentDetailViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.respect.RespectAssignment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class RespectAssignmentEditUiState(
    var assignment: RespectAssignment? = null,
)

class RespectAssignmentEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadEditViewModel(
    di, savedStateHandle, DEST_NAME
) {


    private val _uiState = MutableStateFlow(RespectAssignmentEditUiState())

    val uiState: Flow<RespectAssignmentEditUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.save),
                    onClick = this@RespectAssignmentEditViewModel::onClickSave,
                ),
            )
        }

        viewModelScope.launch {
            loadEntity(
                serializer = RespectAssignment.serializer(),
                onLoadFromDb = {
                    it.respectAssignmentDao().findByUidAsync(entityUidArg)
                },
                makeDefault = {
                    RespectAssignment(
                        razToClazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: -1,
                        razRlUid = savedStateHandle[ARG_RESPECT_LESSON_UID]?.toLong() ?: -1,
                        razAssignedByPersonUid = activeUserPersonUid,
                    )
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(assignment = it)
                    }
                }
            )
        }
    }

    fun onEntityChanged(entity: RespectAssignment?) {
        _uiState.update { prev -> prev.copy(assignment = entity) }
    }

    fun onClickSave() {
        val entity = _uiState.value.assignment ?: return

        viewModelScope.launch {
            activeRepo.withDoorTransactionAsync {
                activeRepo.respectAssignmentDao().upsertAsync(entity)
            }

            finishWithResult(
                detailViewName = RespectAssignmentDetailViewModel.DEST_NAME,
                entityUid = entity.razUid,
                result = entity,
            )
        }
    }

    companion object {

        const val DEST_NAME = "RespectAssignmentEdit"

        const val ARG_TASK_NAME_NEW_ASSIGNMENT = "NewRespectAssignment"

        const val ARG_RESPECT_LESSON_UID = "respectLessonUid"

        val NEW_ASSIGNMENT_ARGS_TO_PASS = listOf(ARG_TASK, ARG_POPUPTO_ON_FINISH, ARG_CLAZZUID)

    }
}