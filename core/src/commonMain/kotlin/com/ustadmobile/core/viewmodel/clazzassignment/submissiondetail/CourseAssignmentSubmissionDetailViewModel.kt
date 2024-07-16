package com.ustadmobile.core.viewmodel.clazzassignment.submissiondetail

import com.ustadmobile.core.domain.assignment.submittername.GetAssignmentSubmitterNameUseCase
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

data class CourseAssignmentSubmissionDetailUiState(
    val submission: CourseAssignmentSubmission? = null,
)

/**
 * Full screen view of one particular submission
 */
class CourseAssignmentSubmissionDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : DetailViewModel<CourseAssignmentSubmission>(di, savedStateHandle, DEST_NAME) {


    private val _uiState = MutableStateFlow(CourseAssignmentSubmissionDetailUiState())

    val uiState: Flow<CourseAssignmentSubmissionDetailUiState> = _uiState.asStateFlow()

    private val getAssignmentSubmitterNameUseCase: GetAssignmentSubmitterNameUseCase by
        on(accountManager.activeEndpoint).instance()

    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                activeRepo.courseAssignmentSubmissionDao().findByUidAsFlow(entityUidArg).collect {
                    val prevState = _uiState.getAndUpdate { prev ->
                        prev.copy(
                            submission = it
                        )
                    }

                    val currentSubmitterUid = it?.casSubmitterUid ?: 0L
                    val prevSubmitterUid = prevState.submission?.casSubmitterUid ?: 0L
                    if(currentSubmitterUid != 0L && prevSubmitterUid == 0L) {
                        viewModelScope.launch {
                            val submitterName = getAssignmentSubmitterNameUseCase.invoke(
                                currentSubmitterUid
                            )
                            _appUiState.update { prev -> prev.copy(title = submitterName) }
                        }
                    }
                }
            }
        }
    }

    companion object {

        const val DEST_NAME = "AssignmentSubmission"

    }

}