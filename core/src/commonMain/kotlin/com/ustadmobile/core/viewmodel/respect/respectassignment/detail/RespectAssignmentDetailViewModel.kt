package com.ustadmobile.core.viewmodel.respect.respectassignment.detail

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.lib.db.entities.respect.RespectAssignment
import com.ustadmobile.lib.db.entities.respect.RespectAssignmentLessonAndApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class RespectAssignmentDetailUiState(
    val assignment: RespectAssignmentLessonAndApp? = null,
)

class RespectAssignmentDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : DetailViewModel<RespectAssignment>(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(RespectAssignmentDetailUiState())

    val uiState: Flow<RespectAssignmentDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            activeRepo.respectAssignmentDao().findByUidAsFlow(entityUidArg).collect {
                _uiState.update { prev ->
                    prev.copy(assignment = it)
                }

                _appUiState.update { prev ->
                    prev.copy(title = it?.assignment?.razTitle ?: "")
                }
            }
        }
    }


    fun onClickLaunch() {

    }

    companion object {

        const val DEST_NAME = "RespectAssignmentDetail"

    }
}