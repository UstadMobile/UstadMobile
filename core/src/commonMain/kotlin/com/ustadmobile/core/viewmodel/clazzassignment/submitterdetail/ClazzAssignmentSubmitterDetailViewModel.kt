package com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail

import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import kotlin.math.max

/**
 *
 * @param submitMarkError Error message to be shown for mark textfield e.g. if not a valid number etc
 * @param submissionList a list of the submissions from this submitter (group or student)
 * @param submissionAttachments attachments for the submissions (if any)
 * @param marks Marks for this submitter that have been recorded (e.g. they are in the database)
 * @param draftMark mark for the given submitter that is being edited/drafted on screen (e.g. for
 * the currently active user to submit)
 * @param markNextStudentVisible if true, show a button for the marker to record the mark for the
 * current submitter and move to the next submission that requires marking.
 * @param markListFilterChipsVisible If there are previous (superceded) grades, the use should see
 * filter chips with the option to see all, or only the latest.
 * @param privateCommentsList list of private comments for this submitter
 * @param newPrivateCommentText private comment text currently being drafted by user on screen
 */
data class ClazzAssignmentSubmitterDetailUiState(

    val submitMarkError: String? = null,

    val courseBlock: CourseBlock? = null,

    val gradeFilterChips: List<ListFilterIdOption> = emptyList(),

    val submissionList: List<CourseAssignmentSubmission> = emptyList(),

    val submissionAttachments: List<CourseAssignmentSubmissionAttachment> = emptyList(),

    val marks: List<CourseAssignmentMarkAndMarkerName> = emptyList(),

    val draftMark: CourseAssignmentMark? = null,

    val markNextStudentVisible: Boolean =  true,

    val fieldsEnabled: Boolean = true,

    val markListFilterChipsVisible: Boolean = true,

    val markListSelectedChipId: Int = CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES,

    val markListFilterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MessageID.most_recent, CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES),
        MessageIdOption2(MessageID.all, CourseAssignmentMarkDaoCommon.ARG_FILTER_ALL_SCORES)
    ),

    val privateCommentsList: ListPagingSourceFactory<CommentsAndName> = { EmptyPagingSource() },

    val newPrivateCommentText: String = "",

    val activeUserPersonUid: Long = 0,

) {

    val submissionStatus: Int
        get() {
            return when {
                marks.isNotEmpty() -> CourseAssignmentSubmission.MARKED
                submissionList.isNotEmpty() -> CourseAssignmentSubmission.SUBMITTED
                else -> CourseAssignmentSubmission.NOT_SUBMITTED
            }
        }

    private val latestUniqueMarksByMarker: List<CourseAssignmentMarkAndMarkerName>
        get() = marks.filter { markWithMarker ->
            val mostRecentTsForSubmitterUid = marks.filter {
                it.courseAssignmentMark?.camMarkerSubmitterUid == markWithMarker.courseAssignmentMark?.camMarkerSubmitterUid
            }.maxOf { it.courseAssignmentMark?.camLct ?: 0 }

            markWithMarker.courseAssignmentMark?.camLct ==mostRecentTsForSubmitterUid
        }

    val averageScore: Float
        get() {
            return latestUniqueMarksByMarker.let {
                it.sumOf { it.courseAssignmentMark?.camMark?.toDouble() ?: 0.0 }.toFloat() / max(it.size, 1)
            }
        }

    fun markListItemUiState(
        mark: CourseAssignmentMarkAndMarkerName
    ): UstadCourseAssignmentMarkListItemUiState {
        return UstadCourseAssignmentMarkListItemUiState(
            mark
        )
    }

}

/**
 * Shows a list of the submissions, grades, and comments for any given submitter. This screen is
 * where a teacher or peer can record a mark for a submitter.
 */
class ClazzAssignmentSubmitterDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): DetailViewModel<CourseAssignmentSubmission>(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ClazzAssignmentSubmitterDetailUiState())

    val uiState: Flow<ClazzAssignmentSubmitterDetailUiState> = _uiState.asStateFlow()

    private val assignmentUid = savedStateHandle[ARG_ASSIGNMENT_UID]?.toLong()
        ?: throw IllegalArgumentException("No assignmentUid")

    private val submitterUid = savedStateHandle[ARG_SUBMITTER_UID]?.toLong()
        ?: throw IllegalArgumentException("No submitter uid")

    private val privateCommentsPagingSourceFactory: ListPagingSourceFactory<CommentsAndName> = {
        activeRepo.commentsDao.findPrivateCommentsForSubmitterByAssignmentUid(
            submitterUid = submitterUid,
            assignmentUid = assignmentUid
        )
    }

    init {
        viewModelScope.launch {
            _uiState.update { prev ->
                prev.copy(
                    privateCommentsList = privateCommentsPagingSourceFactory,
                    activeUserPersonUid = activeUserPersonUid,
                )
            }

            _uiState.whenSubscribed {
                launch {
                    activeRepo.courseBlockDao.findCourseBlockByAssignmentUid(assignmentUid).collect {
                        _uiState.update { prev ->
                            prev.copy(courseBlock = it)
                        }
                    }
                }

                launch {
                    activeRepo.courseAssignmentSubmissionDao.getAllSubmissionsFromSubmitterAsFlow(
                        submitterUid = submitterUid,
                        assignmentUid = assignmentUid
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(submissionList = it)
                        }
                    }
                }

                launch {
                    activeRepo.courseAssignmentMarkDao.getAllMarksForSubmitterAsFlow(
                        submitterUid= submitterUid,
                        assignmentUid = assignmentUid
                    ).collect{
                        _uiState.update { prev ->
                            prev.copy(marks = it)
                        }
                    }
                }
            }
        }
    }

    companion object {

        const val ARG_ASSIGNMENT_UID = "assignmentUid"

        const val ARG_SUBMITTER_UID = "submitterUid"

        const val DEST_NAME = "CourseAssignmentSubmitter"

    }

}