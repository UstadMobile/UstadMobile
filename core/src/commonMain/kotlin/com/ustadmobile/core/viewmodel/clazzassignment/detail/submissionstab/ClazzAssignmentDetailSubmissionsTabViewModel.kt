package com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab

import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon
import com.ustadmobile.core.MR
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
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazz.launchSetTitleFromClazzUid
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
            SortOrderOption(MR.strings.name_key, ClazzAssignmentDaoCommon.SORT_NAME_ASC, true),
            SortOrderOption(MR.strings.name_key, ClazzAssignmentDaoCommon.SORT_NAME_DESC, false),
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

    private val argClazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong()
        ?: throw IllegalArgumentException("No ClazzUid provided")

    private val pagingSourceFactory: ListPagingSourceFactory<AssignmentSubmitterSummary> = {
        activeRepoWithFallback.clazzAssignmentDao().getAssignmentSubmitterSummaryListForAssignment(
            assignmentUid = argEntityUid,
            clazzUid = argClazzUid,
            accountPersonUid = activeUserPersonUid,
            group = systemImpl.getString(MR.strings.group),
            searchText = _appUiState.value.searchState.searchText.toQueryLikeParam(),
            sortOption = _uiState.value.sortOption.flag,
        )
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
                val terminology = activeRepoWithFallback.courseTerminologyDao()
                    .getTerminologyForAssignment(argEntityUid)
                _uiState.update { prev ->
                    prev.copy(
                        courseTerminology = terminology
                    )
                }
            }

            _uiState.whenSubscribed {
                launch {
                    activeRepoWithFallback.clazzAssignmentDao().getProgressSummaryForAssignment(
                        assignmentUid = argEntityUid,
                        clazzUid = argClazzUid,
                        accountPersonUid = activeUserPersonUid,
                        group = systemImpl.getString(MR.strings.group)
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                progressSummary = it
                            )
                        }
                    }
                }

                launchSetTitleFromClazzUid(argClazzUid) { title ->
                    _appUiState.update { it.copy(title = title) }
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
                ARG_CLAZZUID to argClazzUid.toString(),
            )
        )
    }

    fun onChangeSortOption(sortOrderOption: SortOrderOption) {
        _uiState.update { prev ->
            prev.copy(
                sortOption = sortOrderOption
            )
        }
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    override fun onUpdateSearchResult(searchText: String) {
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    override fun onClickAdd() {
        //Do nothing - there is no add submitter - that is controlled by groups/enrolment
    }

    companion object {

        const val DEST_NAME = "CourseAssignmentSubmissionsTab"
    }

}