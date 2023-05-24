package com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import kotlin.jvm.JvmInline

data class ClazzAssignmentDetailSubmissionsTabUiState(

    val progressSummary: AssignmentProgressSummary? = null,

    val assignmentSubmitterList: ListPagingSourceFactory<AssignmentSubmitterSummary> = {
        EmptyPagingSource()
    }

)

val AssignmentSubmitterSummary.listItemUiState
    get() = AssignmentSubmitterSummaryUiState(this)

@JvmInline
value class AssignmentSubmitterSummaryUiState(
    val person: AssignmentSubmitterSummary,
) {

    val fileSubmissionStatusIconVisible: Boolean
        get() = person.fileSubmissionStatus != CourseAssignmentSubmission.NOT_SUBMITTED

    val submissionStatusTextVisible: Boolean
        get() = person.fileSubmissionStatus != 0

    val latestPrivateCommentVisible: Boolean
        get() = !person.latestPrivateComment.isNullOrBlank()

}

class ClazzAssignmentDetailSubmissionsTabViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadListViewModel<ClazzAssignmentDetailSubmissionsTabUiState>(
    di = di,
    savedStateHandle = savedStateHandle,
    initialState = ClazzAssignmentDetailSubmissionsTabUiState(),
    destinationName = ClazzAssignmentDetailViewModel.DEST_NAME
){

    private val argEntityUid = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L

    private var mLastPagingSource: PagingSource<Int, AssignmentSubmitterSummary>? = null

    private val pagingSourceFactory: ListPagingSourceFactory<AssignmentSubmitterSummary> = {
        activeRepo.clazzAssignmentDao.getAssignmentSubmitterSummaryListForAssignment(
            assignmentUid = argEntityUid,
            accountPersonUid = activeUserPersonUid,
            group = systemImpl.getString(MessageID.group)
        ).also {
            mLastPagingSource?.invalidate()
            mLastPagingSource = it
        }
    }

    init {
        _uiState.update { prev ->
            prev.copy(
                assignmentSubmitterList = pagingSourceFactory
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.clazzAssignmentDao.getProgressSummaryForAssignment(
                        assignmentUid = argEntityUid,
                        accountPersonUid = activeUserPersonUid,
                        group = systemImpl.getString(MessageID.group)
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                progressSummary = it
                            )
                        }
                    }
                }

                launch {
                    activeRepo.courseBlockDao.getTitleByAssignmentUid(argEntityUid).collect {
                        _appUiState.update { prev ->
                            prev.copy(title = it)
                        }
                    }
                }
            }
        }
    }

    fun onClickSubmitter(assignmentSubmitter: AssignmentSubmitterSummary) {

    }

    override fun onUpdateSearchResult(searchText: String) {

    }

    override fun onClickAdd() {

    }

    companion object {

        const val DEST_NAME = "CourseAssignmentSubmissionsTab"
    }

}