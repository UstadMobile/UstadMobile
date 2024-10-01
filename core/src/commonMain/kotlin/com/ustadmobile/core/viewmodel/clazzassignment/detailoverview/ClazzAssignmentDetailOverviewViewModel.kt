package com.ustadmobile.core.viewmodel.clazzassignment.detailoverview

import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon
import com.ustadmobile.core.domain.assignment.submitassignment.SubmitAssignmentUseCase
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import app.cash.paging.PagingSource
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUiUseCase
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUseCase
import com.ustadmobile.core.domain.blob.openblob.OpeningBlobState
import com.ustadmobile.core.domain.blob.upload.CancelBlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.saveandupload.SaveAndUploadLocalUrisUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.util.ext.toggle
import com.ustadmobile.core.viewmodel.clazz.launchSetTitleFromClazzUid
import com.ustadmobile.core.viewmodel.clazzassignment.asBlobOpenItem
import com.ustadmobile.core.viewmodel.clazzassignment.averageMark
import com.ustadmobile.core.viewmodel.clazzassignment.combineWithSubmissionFiles
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.hasUpdatedMarks
import com.ustadmobile.core.viewmodel.clazzassignment.latestUniqueMarksByMarker
import com.ustadmobile.core.viewmodel.clazzassignment.submissionStatusFor
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.composites.CourseAssignmentSubmissionFileAndTransferJob
import com.ustadmobile.lib.db.composites.SubmissionAndFiles
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.instanceOrNull


/**
 *
 */
data class ClazzAssignmentDetailOverviewUiState(

    val assignment: ClazzAssignment? = null,

    val courseBlock: CourseBlock? = null,

    val courseBlockPicture: CourseBlockPicture? = null,

    val courseGroupSet: CourseGroupSet? = null,

    /**
     * The submitter uid of the active user - see CourseAssignmentSubmission.casSubmitterUid
     */
    val submitterUid: Long = 0,

    val editableSubmissionFiles: List<CourseAssignmentSubmissionFileAndTransferJob> = emptyList(),

    val submissionTooLong: Boolean = false,

    val submissions: List<SubmissionAndFiles> = emptyList(),

    val markList: List<CourseAssignmentMarkAndMarkerName> = emptyList(),

    val courseComments: () -> PagingSource<Int, CommentsAndName> = { EmptyPagingSource() },

    val privateComments: () -> PagingSource<Int, CommentsAndName> = { EmptyPagingSource() },

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES,

    val gradeFilterChips: List<MessageIdOption2> = listOf(
        MessageIdOption2(MR.strings.most_recent, CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES),
        MessageIdOption2(MR.strings.all, CourseAssignmentMarkDaoCommon.ARG_FILTER_ALL_SCORES)
    ),

    val submissionHeaderUiState: UstadAssignmentSubmissionHeaderUiState =
        UstadAssignmentSubmissionHeaderUiState(),

    val unassignedError: String? = null,

    val submissionError: String? = null,

    val activeUserPersonUid: Long = 0,

    val activeUserPersonName: String = "",

    val activeUserPictureUri: String? = null,

    val courseTerminology: CourseTerminology? = null,

    val localDateTimeNow: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()),

    val dayOfWeekStringMap: Map<DayOfWeek, String> = emptyMap(),

    val collapsedSubmissions: Set<Long> = emptySet(),

    /**
     * If/when the user is opening a file submission, this is the progress and info. On desktop and
     * Android this is used to show status in a bottom sheet (not applicable on web where blob
     * download is done via browser).
     */
    val openingFileSubmissionState: OpeningBlobState? = null,

    val showModerateOptions: Boolean = false,

) {

    val caDescriptionVisible: Boolean
        get() = !courseBlock?.cbDescription.isNullOrBlank()

    val cbDeadlineDateVisible: Boolean
        get() = courseBlock?.cbDeadlineDate.isDateSet()

    val submitSubmissionButtonVisible: Boolean
        get() = activeUserCanSubmit

    val unassignedErrorVisible: Boolean
        get() = !unassignedError.isNullOrBlank()

    val showClassComments: Boolean
        get() = assignment?.caClassCommentEnabled == true

    val activeUserIsSubmitter: Boolean
        get() = submitterUid > 0L

    val showPrivateComments: Boolean
        get() = activeUserIsSubmitter && assignment?.caPrivateCommentsEnabled == true

    val isWithinDeadlineOrGracePeriod: Boolean
        get() {
            val timeNow = systemTimeInMillis()
            if(timeNow < (courseBlock?.cbDeadlineDate ?: UNSET_DISTANT_FUTURE)) {
                return true
            }
            if(courseBlock?.cbGracePeriodDate.isDateSet() &&
                timeNow <= (courseBlock?.cbGracePeriodDate ?: 0L)
            ) {
                return true
            }

            return false
        }

    val activeUserCanSubmit: Boolean
        get() {
            if(!activeUserIsSubmitter)
                return false

            if(!isWithinDeadlineOrGracePeriod)
                return false

            if(assignment?.caSubmissionPolicy == ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE &&
                submissions.isNotEmpty()
            ) {
                return false
            }

            return true
        }


    val canEditSubmissionText: Boolean
        get() = activeUserCanSubmit && assignment?.caRequireTextSubmission == true

    val addFileSubmissionVisible: Boolean
        get() = activeUserCanSubmit && assignment?.caRequireFileSubmission == true &&
                editableSubmissionFiles.size < assignment.caNumberOfFiles

    val submissionStatus: Int?
        get() {
            return if(activeUserIsSubmitter)
                submissionStatusFor(markList, submissions)
            else
                null
        }

    val privateCommentSectionVisible: Boolean
        get() = activeUserIsSubmitter && unassignedError == null

    val submitPrivateCommentVisible: Boolean
        get() = privateCommentSectionVisible && assignment?.caPrivateCommentsEnabled == true

    val pointsVisible: Boolean
        get() = submissionMark != null

    val latePenaltyVisible: Boolean
        get() = submissionMark.let { it  != null && it.averagePenalty != 0 }

    /**
     * Submission text field will be visible when text submission is required, active user is a submitter,
     * and the latest submission is not null (entity will be set by viewmodel as part of loading process).
     */
    val submissionTextFieldVisible: Boolean
        get() = assignment?.caRequireTextSubmission == true && activeUserCanSubmit

    private val latestUniqueMarksByMarker: List<CourseAssignmentMarkAndMarkerName>
        get() = markList.latestUniqueMarksByMarker()

    val visibleMarks: List<CourseAssignmentMarkAndMarkerName>
        get() = if(selectedChipId == CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES) {
            latestUniqueMarksByMarker
        }else {
            markList
        }

    val gradeFilterChipsVisible: Boolean
        get() = markList.hasUpdatedMarks()

    val submissionMark: AverageCourseAssignmentMark?
        get() = markList.averageMark()

    val isGroupSubmission: Boolean
        get() = assignment?.caGroupUid?.let { it != 0L } ?: false

}

/**
 * Assignment text editing and comment editing takes place inside a VirtualList. The Virtual List is
 * not able to deliver changes synchronously, so the state must be separated out so the child
 * component can consume it independently of the main UI state.
 */
data class ClazzAssignmentDetailoverviewSubmissionUiState(
    val editableSubmission: CourseAssignmentSubmission? = null,
)

class ClazzAssignmentDetailOverviewViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    private val submitAssignmentUseCase: SubmitAssignmentUseCase = SubmitAssignmentUseCase(
        systemImpl = di.direct.instance(),
    ),
) : DetailViewModel<ClazzAssignment>(
    di, savedStateHandle, ClazzAssignmentDetailViewModel.DEST_NAME
){

    private val _uiState = MutableStateFlow(
        ClazzAssignmentDetailOverviewUiState(
            dayOfWeekStringMap = systemImpl.getDayOfWeekStrings()
        )
    )

    val uiState: Flow<ClazzAssignmentDetailOverviewUiState> = _uiState.asStateFlow()

    private val _editableSubmissionUiState = MutableStateFlow(
        ClazzAssignmentDetailoverviewSubmissionUiState()
    )

    val editableSubmissionUiState: Flow<ClazzAssignmentDetailoverviewSubmissionUiState> = _editableSubmissionUiState.asStateFlow()

    private val _newPrivateCommentText = MutableStateFlow("")

    val newPrivateCommentText: Flow<String> = _newPrivateCommentText.asStateFlow()

    private val _newCourseCommentText = MutableStateFlow("")

    val newCourseCommentText: Flow<String> = _newCourseCommentText.asStateFlow()

    private val privateCommentsPagingSourceFactory: () -> PagingSource<Int, CommentsAndName> = {
        activeRepoWithFallback.commentsDao().findPrivateCommentsForUserByAssignmentUid(
            accountPersonUid = activeUserPersonUid,
            assignmentUid = entityUidArg,
            includeDeleted = false,
        )
    }

    private val courseCommentsPagingSourceFactory: () -> PagingSource<Int, CommentsAndName> = {
        activeRepoWithFallback.commentsDao().findCourseCommentsByAssignmentUid(
            assignmentUid = entityUidArg,
            includeDeleted = false,
        )
    }

    private var savedSubmissionJob: Job? = null

    private val clazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: throw IllegalArgumentException("clazzUid arg is required")

    private val saveAndUploadUseCase: SaveAndUploadLocalUrisUseCase by di.onActiveEndpoint()
        .instance()

    private val cancelTransferJobUseCase: CancelBlobUploadClientUseCase? by di.onActiveEndpoint()
        .instanceOrNull()

    private val openBlobUiUseCase: OpenBlobUiUseCase? by di.onActiveEndpoint().instanceOrNull()

    private var openBlobJob: Job? = null

    init {
        _uiState.update { prev ->
            prev.copy(
                activeUserPersonUid = activeUserPersonUid,
                activeUserPersonName = accountManager.currentUserSession.person.fullName(),
                activeUserPictureUri = accountManager.currentUserSession.personPicture?.personPictureUri,
            )
        }

        val entityFlow = activeRepoWithFallback
            .clazzAssignmentDao().findAssignmentCourseBlockAndSubmitterUidAsFlow(
                assignmentUid = entityUidArg,
                clazzUid = clazzUid,
                accountPersonUid = accountManager.currentAccount.personUid,
            ).shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        viewModelScope.launch {
            val editableSubmission = savedStateHandle.getJson(
                STATE_EDITABLE_SUBMISSION, CourseAssignmentSubmission.serializer(),
            ) ?: newCourseAssignmentSubmission()

            _editableSubmissionUiState.update { prev -> prev.copy(editableSubmission = editableSubmission) }

            launch {
                navResultReturner.filteredResultFlowForKey(KEY_SUBMISSION_HTML).collect {
                    val newSubmissionHtml = it.result as? String ?: return@collect
                    onChangeSubmissionText(newSubmissionHtml)
                }
            }

            _uiState.whenSubscribed {
                launch {
                    entityFlow.collect { assignmentData ->
                        _uiState.update { prev ->
                            val isEnrolledButNotInGroup = assignmentData?.submitterUid ==
                                CourseAssignmentSubmission.SUBMITTER_ENROLLED_BUT_NOT_IN_GROUP
                            prev.copy(
                                assignment = assignmentData?.clazzAssignment,
                                courseBlock = assignmentData?.courseBlock,
                                courseBlockPicture = assignmentData?.courseBlockPicture,
                                submitterUid = assignmentData?.submitterUid ?: 0,
                                courseGroupSet = assignmentData?.courseGroupSet,
                                unassignedError = if(isEnrolledButNotInGroup) {
                                    systemImpl.getString(MR.strings.unassigned_error)
                                }else {
                                    null
                                },
                                showModerateOptions = assignmentData?.hasModeratePermission ?: false,
                            )
                        }
                    }
                }

                launchSetTitleFromClazzUid(clazzUid) { title ->
                    _appUiState.update { it.copy(title = title) }
                }

                launch {
                    val submissionFlow = activeRepoWithFallback
                        .courseAssignmentSubmissionDao().findByAssignmentUidAndAccountPersonUid(
                            accountPersonUid = activeUserPersonUid,
                            assignmentUid = entityUidArg,
                        )

                    val submissionFilesFlow = activeRepoWithFallback
                        .courseAssignmentSubmissionFileDao().getByAssignmentUidAndPersonUid(
                            accountPersonUid = activeUserPersonUid,
                            assignmentUid = entityUidArg,
                        )

                    submissionFlow.combine(submissionFilesFlow) { submissions, submissionFiles ->
                        submissions.combineWithSubmissionFiles(submissionFiles)
                    }.distinctUntilChanged().collect {
                        _uiState.update { prev ->
                            prev.copy(submissions = it)
                        }
                    }
                }

                launch {
                    activeRepoWithFallback.courseAssignmentMarkDao().getAllMarksForUserAsFlow(
                        accountPersonUid = activeUserPersonUid,
                        assignmentUid = entityUidArg
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                markList = it
                            )
                        }
                    }
                }

                launch {
                    //Note: the submission uid will change when the user submits.
                    _editableSubmissionUiState.map {
                        it.editableSubmission?.casUid ?: 0
                    }.distinctUntilChanged().collectLatest { submissionUid ->
                        activeDb.courseAssignmentSubmissionFileDao().getBySubmissionUid(
                            submissionUid
                        ).distinctUntilChanged().collect {
                            _uiState.update { prev -> prev.copy(editableSubmissionFiles = it) }
                        }
                    }
                }
            }
        }

        _uiState.update { prev ->
            prev.copy(
                privateComments = privateCommentsPagingSourceFactory,
                courseComments = courseCommentsPagingSourceFactory,
            )
        }
    }

    /**
     * Create a new (blank) assignment submission. Used on instantiation and after a submission is
     * made.
     */
    private fun newCourseAssignmentSubmission() = CourseAssignmentSubmission(
        casUid = activeDb.doorPrimaryKeyManager.nextId(CourseAssignmentSubmission.TABLE_ID),
        casAssignmentUid = entityUidArg,
        casClazzUid = clazzUid,
        casSubmitterPersonUid = accountManager.currentUserSession.person.personUid,
        casText = "",
    )


    fun onClickMarksFilterChip(option: MessageIdOption2) {
        _uiState.update { prev ->
            prev.copy(
               selectedChipId =  option.value,
            )
        }
    }

    /**
     * Used on mobile to bring user to a new screen to edit submission
     */
    fun onClickEditSubmissionText() {
        navigateToEditHtml(
            currentValue = _editableSubmissionUiState.value.editableSubmission?.casText ?: "",
            resultKey = KEY_SUBMISSION_HTML
        )
    }

    fun onChangeSubmissionText(text: String) {
        val submissionToSave = _editableSubmissionUiState.updateAndGet { prev ->
            prev.copy(
                editableSubmission = prev.editableSubmission?.shallowCopy {
                    casText = text
                }
            )
        }.editableSubmission

        savedSubmissionJob?.cancel()
        savedSubmissionJob = viewModelScope.launch {
            delay(200)
            if(submissionToSave != null) {
                savedStateHandle.setJson(
                    key = STATE_EDITABLE_SUBMISSION,
                    serializer = CourseAssignmentSubmission.serializer(),
                    value = submissionToSave
                )
            }
        }

    }

    fun onChangePrivateCommentText(text: String) {
        _newPrivateCommentText.value = text
    }

    fun onClickSubmitPrivateComment() {
        val submitterUid = _uiState.value.submitterUid
        if(submitterUid <= 0)
            //invalid - this should never happen because private comment field would not be visible
            return


        if(loadingState == LoadingUiState.INDETERMINATE)
            return

        loadingState = LoadingUiState.INDETERMINATE

        viewModelScope.launch {
            try {
                activeRepoWithFallback.commentsDao().insertAsync(Comments().apply {
                    commentsForSubmitterUid = submitterUid
                    commentsFromPersonUid = activeUserPersonUid
                    commentsFromSubmitterUid = _uiState.value.submitterUid
                    commentsEntityUid = entityUidArg
                    commentsText = _newPrivateCommentText.value
                    commentsDateTimeAdded = systemTimeInMillis()
                })
                _newPrivateCommentText.value = ""
            }finally {
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }

    fun onChangeCourseCommentText(text: String) {
        _newCourseCommentText.value = text
    }

    fun onClickSubmitCourseComment() {
        if(loadingState == LoadingUiState.INDETERMINATE)
            return

        loadingState = LoadingUiState.INDETERMINATE
        viewModelScope.launch {
            try {
                activeRepoWithFallback.commentsDao().insertAsync(Comments().apply {
                    commentsForSubmitterUid = 0
                    commentsFromPersonUid = activeUserPersonUid
                    commentsEntityUid = entityUidArg
                    commentsText = _newCourseCommentText.value
                    commentsDateTimeAdded = systemTimeInMillis()
                })

                _newCourseCommentText.value = ""
            }finally {
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }

    fun onAddSubmissionFile(
        uri: String,
        fileName: String,
        mimeType: String,
        size: Long,
    ) {
        val assignmentSizeLimit = _uiState.value.assignment?.caSizeLimit ?: 0
        if(size > (assignmentSizeLimit * 1024 * 1024)) {
            snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.import_link_big_size)))
            return
        }

        viewModelScope.launch {
            val newAttachment = CourseAssignmentSubmissionFile(
                casaUid = activeDb.doorPrimaryKeyManager.nextIdAsync(
                    CourseAssignmentSubmissionFile.TABLE_ID
                ),
                casaSubmissionUid = _editableSubmissionUiState.value.editableSubmission?.casUid ?: 0,
                casaFileName = fileName,
                casaSubmitterUid = _uiState.value.submitterUid,
                casaMimeType = mimeType,
                casaSize = size.toInt(),
                casaUri = uri,
                casaCaUid = entityUidArg,
                casaClazzUid = clazzUid,
            )

            activeDb.courseAssignmentSubmissionFileDao().insertListAsync(listOf(newAttachment))

            try {
                saveAndUploadUseCase(
                    listOf(
                        SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = uri,
                            entityUid = newAttachment.casaUid,
                            tableId = CourseAssignmentSubmissionFile.TABLE_ID,
                            mimeType = mimeType,
                            createRetentionLock = true,
                        )
                    )
                )
            }catch(e: Throwable) {
                Napier.w("WARNING: Exception attempting to save/enqueue submission file", e)
                //Give the user bad news
            }
        }
    }


    fun onClickSubmit() {
        if(!_uiState.value.fieldsEnabled)
            return

        val submission = _editableSubmissionUiState.value.editableSubmission ?: return

        _uiState.update { prev -> prev.copy(fieldsEnabled = false) }
        loadingState = LoadingUiState.INDETERMINATE

        viewModelScope.launch {
            try {
                submitAssignmentUseCase(
                    repo = activeRepoWithFallback,
                    submitterUid = _uiState.value.submitterUid,
                    assignmentUid = entityUidArg,
                    accountPersonUid = accountManager.currentUserSession.person.personUid,
                    submission = submission
                )

                val submissionToSave = _editableSubmissionUiState.updateAndGet { prev ->
                    prev.copy(
                        editableSubmission = newCourseAssignmentSubmission(),
                    )
                }.editableSubmission

                _uiState.update { it.copy(submissionError = null) }

                if(submissionToSave != null) {
                    savedStateHandle.setJson(
                        key = STATE_EDITABLE_SUBMISSION,
                        serializer = CourseAssignmentSubmission.serializer(),
                        value = submissionToSave,
                    )
                }

                snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.submitted_key)))
            }catch(e: Exception) {
                Napier.e("Exception submitting assignment: $e", e)
                _uiState.update { prev ->
                    prev.copy(submissionError = e.message)
                }
            }finally {
                _uiState.update { prev -> prev.copy(fieldsEnabled = true) }
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }

    fun onToggleSubmissionExpandCollapse(submission: CourseAssignmentSubmission) {
        _uiState.update { prev ->
            prev.copy(
                collapsedSubmissions = prev.collapsedSubmissions.toggle(submission.casUid)
            )
        }
    }

    fun onClickCourseGroupSet() {
        navController.navigate(
            viewName = CourseGroupSetDetailViewModel.DEST_NAME,
            args = mapOf(
                ARG_ENTITY_UID to (_uiState.value.courseGroupSet?.cgsUid?.toString() ?: "0"),
                ARG_CLAZZUID to clazzUid.toString(),
            )
        )
    }

    fun onRemoveSubmissionFile(file: CourseAssignmentSubmissionFileAndTransferJob) {
        viewModelScope.launch {
            activeRepoWithFallback.withDoorTransactionAsync {
                activeRepoWithFallback.courseAssignmentSubmissionFileDao().setDeleted(
                    casaUid = file.submissionFile?.casaUid ?: 0,
                    deleted = true,
                    updateTime = systemTimeInMillis(),
                )
            }

            file.transferJobItem?.tjiTjUid?.also { transferJobId ->
                cancelTransferJobUseCase?.invoke(transferJobId)
            }
        }
    }

    fun onSendSubmissionFile(file: CourseAssignmentSubmissionFileAndTransferJob) {
        openSubmissionFileAsBlob(file, OpenBlobUseCase.OpenBlobIntent.SEND)
    }

    fun onOpenSubmissionFile(file: CourseAssignmentSubmissionFileAndTransferJob) {
        openSubmissionFileAsBlob(file, OpenBlobUseCase.OpenBlobIntent.VIEW)
    }

    private fun openSubmissionFileAsBlob(
        file: CourseAssignmentSubmissionFileAndTransferJob,
        intent: OpenBlobUseCase.OpenBlobIntent,
    ){
        val submissionFile = file.submissionFile ?: return
        openBlobJob?.cancel()

        openBlobJob = viewModelScope.launch {
            openBlobUiUseCase?.invoke(
                openItem = submissionFile.asBlobOpenItem(),
                onUiUpdate = {
                    _uiState.update { prev -> prev.copy(openingFileSubmissionState = it) }
                },
                intent = intent,
            )
        }
    }

    fun onDismissOpenFileSubmission() {
        openBlobJob?.cancel()
        _uiState.update { prev ->
            prev.copy(
                openingFileSubmissionState = null,
            )
        }
    }

    fun onDeleteComment(comments: Comments) {
        viewModelScope.launch {
            activeRepoWithFallback.commentsDao().updateDeletedByCommentUid(
                uid = comments.commentsUid,
                deleted = true,
                changeTime = systemTimeInMillis()
            )
            snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.deleted)))
        }
    }


    companion object {

        const val STATE_EDITABLE_SUBMISSION = "latestSubmission"

        const val KEY_SUBMISSION_HTML = "submissionHtml"

        const val DEST_NAME = "CourseAssignmentDetailOverviewView"

    }
}
