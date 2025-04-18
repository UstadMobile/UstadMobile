package com.ustadmobile.core.viewmodel.respect.respectassignment.detail

import app.cash.paging.PagingSource
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.respect.RespectLaunchUseCase
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.PersonAndClazzMemberListDetails
import com.ustadmobile.lib.db.entities.respect.EnrolmentAndPerson
import com.ustadmobile.lib.db.entities.respect.RespectAssignment
import com.ustadmobile.lib.db.entities.respect.RespectAssignmentLessonAndApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instanceOrNull

data class RespectAssignmentDetailUiState(
    val assignment: RespectAssignmentLessonAndApp? = null,
    val assignees: () -> PagingSource<Int, EnrolmentAndPerson> = { EmptyPagingSource() }
)

class RespectAssignmentDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : DetailViewModel<RespectAssignment>(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(RespectAssignmentDetailUiState())

    val uiState: Flow<RespectAssignmentDetailUiState> = _uiState.asStateFlow()

    private val launchUseCase: RespectLaunchUseCase? by di.onActiveEndpoint().instanceOrNull()

    private val assigneesPagingSource: () -> PagingSource<Int, EnrolmentAndPerson> = {
        activeRepo.clazzEnrolmentDao().findByRespectAssignmentUid(entityUidArg)
    }

    init {
        _uiState.update { it.copy(assignees = assigneesPagingSource) }

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
        val assignmentVal = _uiState.value.assignment ?: return

        viewModelScope.launch {
            launchUseCase?.invoke(
                app = assignmentVal.app,
                lesson = assignmentVal.lesson,
                assignment = assignmentVal.assignment,
            )
        }


    }

    companion object {

        const val DEST_NAME = "RespectAssignmentDetail"

    }
}