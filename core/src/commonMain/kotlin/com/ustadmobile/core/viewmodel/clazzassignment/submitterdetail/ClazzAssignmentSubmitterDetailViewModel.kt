package com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail

import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon
import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES
import com.ustadmobile.core.domain.assignment.submitmark.SubmitMarkUseCase
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import app.cash.paging.PagingSource
import com.ustadmobile.core.domain.assignment.submittername.GetAssignmentSubmitterNameUseCase
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.viewmodel.clazzassignment.hasUpdatedMarks
import com.ustadmobile.core.viewmodel.clazzassignment.latestUniqueMarksByMarker
import com.ustadmobile.core.viewmodel.clazzassignment.submissionStatusFor
import com.ustadmobile.core.viewmodel.clazzassignment.submissiondetail.CourseAssignmentSubmissionDetailViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.*
import dev.icerock.moko.resources.StringResource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
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

    /**
     * Whether or not the mark fields (e.g. score itself, comment, and button) are enabled. This is
     * independent of comments
     */
    val markFieldsEnabled: Boolean = true,

    val markNextStudentVisible: Boolean =  true,

    val fieldsEnabled: Boolean = true,

    val markListSelectedChipId: Int = ARG_FILTER_RECENT_SCORES,

    val markListFilterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MR.strings.most_recent, ARG_FILTER_RECENT_SCORES),
        MessageIdOption2(MR.strings.all, CourseAssignmentMarkDaoCommon.ARG_FILTER_ALL_SCORES)
    ),

    val privateCommentsList: ListPagingSourceFactory<CommentsAndName> = { EmptyPagingSource() },

    val newPrivateCommentTextVisible: Boolean = false,

    val newPrivateCommentText: String = "",

    val activeUserPersonUid: Long = 0,

    /**
     * If the active user
     */
    internal val activeUserSubmitterId: Long = 0,

    val activeUserPersonName: String = "",

    val activeUserPictureUri: String? = null,

) {

    val submissionStatus: Int
        get() {
            return submissionStatusFor(marks, submissionList)
        }

    private val latestUniqueMarksByMarker: List<CourseAssignmentMarkAndMarkerName>
        get() = marks.latestUniqueMarksByMarker()

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

    val submitGradeButtonMessageId: StringResource
        get() = if(marks.any { it.courseAssignmentMark?.camMarkerSubmitterUid == activeUserSubmitterId }) {
            MR.strings.update_grade
        }else {
            MR.strings.submit_grade
        }

    val submitGradeButtonAndGoNextMessageId: StringResource
        get() = if(marks.any { it.courseAssignmentMark?.camMarkerSubmitterUid == activeUserSubmitterId }) {
            MR.strings.update_grade_and_mark_next
        }else {
            MR.strings.submit_grade_and_mark_next
        }

    val visibleMarks: List<CourseAssignmentMarkAndMarkerName>
        get() = if(markListSelectedChipId == ARG_FILTER_RECENT_SCORES) {
            latestUniqueMarksByMarker.sortedByDescending { it.courseAssignmentMark?.camLct ?: 0 }
        }else {
            marks
        }

    val markListFilterChipsVisible: Boolean
        get() = marks.hasUpdatedMarks()

    val scoreSummaryVisible: Boolean
        get() = marks.isNotEmpty()


}

/**
 * Shows a list of the submissions, grades, and comments for any given submitter. This screen is
 * where a teacher or peer can record a mark for a submitter.
 */
class ClazzAssignmentSubmitterDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    private val submitMarkUseCase: SubmitMarkUseCase = SubmitMarkUseCase(),
): DetailViewModel<CourseAssignmentSubmission>(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ClazzAssignmentSubmitterDetailUiState())

    val uiState: Flow<ClazzAssignmentSubmitterDetailUiState> = _uiState.asStateFlow()

    private val assignmentUid = savedStateHandle[ARG_ASSIGNMENT_UID]?.toLong()
        ?: throw IllegalArgumentException("No assignmentUid")

    private val submitterUid = savedStateHandle[ARG_SUBMITTER_UID]?.toLong()
        ?: throw IllegalArgumentException("No submitter uid")

    private val clazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong()
        ?: throw IllegalArgumentException("No clazzUid")

    private val privateCommentsPagingSourceFactory: ListPagingSourceFactory<CommentsAndName> = {
        activeRepo.commentsDao.findPrivateCommentsForSubmitterByAssignmentUid(
            submitterUid = submitterUid,
            assignmentUid = assignmentUid
        ).also {
            lastPrivateCommentsPagingSource = it
        }
    }

    private var lastPrivateCommentsPagingSource: PagingSource<Int, CommentsAndName>? = null

    private val assignmentSubmitterNameUseCase: GetAssignmentSubmitterNameUseCase by
        on(accountManager.activeEndpoint).instance()

    init {
        _uiState.update { prev ->
            prev.copy(
                activeUserPersonName = accountManager.currentUserSession.person.fullName(),
                activeUserPictureUri = accountManager.currentUserSession.personPicture?.personPictureThumbnailUri,
            )
        }

        val permissionFlow = activeRepo.coursePermissionDao
            .userPermissionsForAssignmentSubmitterUid(
                accountPersonUid = activeUserPersonUid,
                assignmentUid = assignmentUid,
                clazzUid = clazzUid,
                submitterUid = submitterUid,
            ).map { permissionPair ->
                permissionPair.firstPermission && permissionPair.secondPermission
            }


        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    permissionFlow.distinctUntilChanged().collectLatest { hasMarkAndViewPermission ->
                        if(hasMarkAndViewPermission) {
                            _uiState.update { prev ->
                                prev.copy(
                                    privateCommentsList = privateCommentsPagingSourceFactory,
                                    activeUserPersonUid = activeUserPersonUid,
                                    draftMark = CourseAssignmentMark().apply {
                                        camMark = (-1).toFloat()
                                    },
                                    newPrivateCommentTextVisible = true,
                                )
                            }

                            launch {
                                val submitterName = assignmentSubmitterNameUseCase.invoke(submitterUid)

                                _appUiState.update { prev ->
                                    prev.copy(
                                        title = submitterName
                                    )
                                }
                            }

                            launch {
                                launch {
                                    activeRepo.courseBlockDao.findCourseBlockByAssignmentUid(
                                        assignmentUid
                                    ).collect {
                                        _uiState.update { prev ->
                                            prev.copy(courseBlock = it)
                                        }
                                    }
                                }
                            }

                            launch {
                                val activeUserSubmitterId = activeRepo.clazzAssignmentDao.getSubmitterUid(
                                    assignmentUid = assignmentUid,
                                    accountPersonUid = activeUserPersonUid,
                                )
                                _uiState.update { prev ->
                                    prev.copy(
                                        activeUserSubmitterId = activeUserSubmitterId
                                    )
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
                                    submitterUid = submitterUid,
                                    assignmentUid = assignmentUid
                                ).collect{
                                    _uiState.update { prev ->
                                        prev.copy(marks = it)
                                    }
                                }
                            }

                        }else {
                            _uiState.update { prev ->
                                prev.copy(
                                    privateCommentsList = { EmptyPagingSource() },
                                    submissionList = emptyList(),
                                    marks = emptyList(),
                                    newPrivateCommentTextVisible = false,
                                    courseBlock = null,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun onChangePrivateComment(text: String) {
        _uiState.update { prev ->
            prev.copy(
                newPrivateCommentText = text
            )
        }
    }

    fun onSubmitPrivateComment() {
        if(loadingState == LoadingUiState.INDETERMINATE)
            return

        loadingState = LoadingUiState.INDETERMINATE
        viewModelScope.launch {
            try {
                activeRepo.commentsDao.insertAsync(Comments().apply {
                    commentSubmitterUid = submitterUid
                    commentsPersonUid = activeUserPersonUid
                    commentsEntityUid = assignmentUid
                    commentsText = _uiState.value.newPrivateCommentText
                    commentsDateTimeAdded = systemTimeInMillis()
                })
                _uiState.update { prev ->
                    prev.copy(newPrivateCommentText = "")
                }
            }finally {
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }

    fun onChangeDraftMark(draftMark: CourseAssignmentMark?) {
        _uiState.update { prev ->
            prev.copy(
                draftMark = draftMark,
                submitMarkError = if(prev.draftMark?.camMark == draftMark?.camMark) {
                    prev.submitMarkError
                }else {
                    null
                }
            )
        }
    }

    fun onClickSubmitMark() {
        if(!_uiState.value.markFieldsEnabled)
            return

        val draftMark = _uiState.value.draftMark ?: return
        val submissions = _uiState.value.submissionList // note: this would be better to check by making it nullable
        val courseBlock = _uiState.value.courseBlock ?: return

        if(draftMark.camMark == (-1).toFloat()) {
            _uiState.update { prev ->
                prev.copy(
                    submitMarkError = systemImpl.getString(MR.strings.field_required_prompt),
                )
            }
            return
        }else if(draftMark.camMark < 0) {
            _uiState.update { prev ->
                prev.copy(
                    submitMarkError = systemImpl.getString(MR.strings.score_greater_than_zero),
                )
            }
            return
        }else if(draftMark.camMark > courseBlock.cbMaxPoints){
            _uiState.update { prev ->
                prev.copy(
                    submitMarkError = systemImpl.getString(MR.strings.too_high),
                )
            }
            return
        }

        _uiState.update { prev -> prev.copy(markFieldsEnabled = false) }

        viewModelScope.launch {
            try {
                submitMarkUseCase(
                    repo = activeRepo,
                    activeUserPersonUid = activeUserPersonUid,
                    assignmentUid = assignmentUid,
                    clazzUid = clazzUid,
                    submitterUid = submitterUid,
                    draftMark = draftMark,
                    submissions = submissions,
                    courseBlock = courseBlock
                )

                _uiState.update { prev ->
                    prev.copy(
                        draftMark = CourseAssignmentMark().apply {
                            camMark = (-1).toFloat()
                        },
                    )
                }
            }catch(e: Exception) {
                snackDispatcher.showSnackBar(Snack("Error: ${e.message}"))
                Napier.w("Exception submitting mark:", e)
            }finally {
                _uiState.update { prev -> prev.copy(markFieldsEnabled = true) }
            }
        }
    }

    fun onClickSubmitMarkAndGoNext() {
        //inactive
    }

    fun onClickSubmission(submission: CourseAssignmentSubmission) {
        navController.navigate(
            viewName = CourseAssignmentSubmissionDetailViewModel.DEST_NAME,
            args = mapOf(
                ARG_ENTITY_UID to submission.casUid.toString()
            )
        )
    }

    fun onClickGradeFilterChip(option: MessageIdOption2) {
        _uiState.update { prev ->
            prev.copy(
                markListSelectedChipId = option.value
            )
        }
    }

    companion object {

        const val ARG_ASSIGNMENT_UID = "assignmentUid"

        const val ARG_SUBMITTER_UID = "submitterUid"

        const val DEST_NAME = "CourseAssignmentSubmitter"

    }

}