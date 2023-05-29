package com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab

import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseTerminology
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import kotlin.jvm.JvmInline

data class ClazzAssignmentDetailSubmissionsTabUiState(

    val progressSummary: AssignmentProgressSummary? = null,

    val assignmentSubmitterList: ListPagingSourceFactory<AssignmentSubmitterSummary> = {
        EmptyPagingSource()
    },

    val sortOptions: List<SortOrderOption> = DEFAULT_SORT_OPTIONS,

    val sortOption: SortOrderOption = sortOptions.first(),

    val courseTerminology: CourseTerminology? = null,

) {
    companion object {

        val DEFAULT_SORT_OPTIONS = listOf(
            SortOrderOption(MessageID.name, ClazzAssignmentDaoCommon.SORT_NAME_ASC, true),
            SortOrderOption(MessageID.name, ClazzAssignmentDaoCommon.SORT_NAME_DESC, false),
        )

    }
}

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
            group = systemImpl.getString(MessageID.group),
            searchText = _appUiState.value.searchState.searchText.toQueryLikeParam(),
            sortOption = _uiState.value.sortOption.flag,
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

        _appUiState.update {prev ->
            prev.copy(
                searchState = createSearchEnabledState()
            )
        }

        viewModelScope.launch {
            launch {
                val terminology = activeRepo.courseTerminologyDao
                    .getTerminologyForAssignment(argEntityUid)
                _uiState.update { prev ->
                    prev.copy(
                        courseTerminology = terminology
                    )
                }
            }

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
        navController.navigate(
            viewName = ClazzAssignmentSubmitterDetailViewModel.DEST_NAME,
            args = mapOf(
                ClazzAssignmentSubmitterDetailViewModel.ARG_ASSIGNMENT_UID to argEntityUid.toString(),
                ClazzAssignmentSubmitterDetailViewModel.ARG_SUBMITTER_UID to assignmentSubmitter.submitterUid.toString(),
            )
        )
    }

    fun onChangeSortOption(sortOrderOption: SortOrderOption) {
        _uiState.update { prev ->
            prev.copy(
                sortOption = sortOrderOption
            )
        }
        mLastPagingSource?.invalidate()
    }

    override fun onUpdateSearchResult(searchText: String) {
        mLastPagingSource?.invalidate()
    }

    override fun onClickAdd() {
        //Do nothing - there is no add submitter - that is controlled by groups/enrolment
    }

    companion object {

        const val DEST_NAME = "CourseAssignmentSubmissionsTab"
    }

}