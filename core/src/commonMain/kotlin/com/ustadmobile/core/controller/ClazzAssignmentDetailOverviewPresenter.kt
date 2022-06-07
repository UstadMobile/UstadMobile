package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.io.ext.guessMimeType
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.TextAssignmentEditView.Companion.EDIT_ENABLED
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.attachments.retrieveAttachment
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.jvm.JvmStatic


class ClazzAssignmentDetailOverviewPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: ClazzAssignmentDetailOverviewView,
    lifecycleOwner: DoorLifecycleOwner,
    di: DI
) : UstadDetailPresenter<ClazzAssignmentDetailOverviewView, ClazzAssignmentWithCourseBlock>(
    context,
    arguments,
    view, di, lifecycleOwner
) {
    
    val statementEndpoint by on(accountManager.activeAccount).instance<XapiStatementEndpoint>()

    val submissionList = mutableListOf<CourseAssignmentSubmissionWithAttachment>()

    // for student/group to enter their private comment about their submission
    val newPrivateCommentListener: DefaultNewCommentItemListener = DefaultNewCommentItemListener(di, context,
        arguments[ARG_ENTITY_UID]?.toLong() ?: 0L,
        ClazzAssignment.TABLE_ID, false)

    // for everyone to enter their public comment
    val newClassCommentListener: DefaultNewCommentItemListener =
        DefaultNewCommentItemListener(di, context,
            arguments[ARG_ENTITY_UID]?.toLong() ?: 0L,
            ClazzAssignment.TABLE_ID, true, 0)

    override val persistenceMode: PersistenceMode
          get() = PersistenceMode.DB

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return false
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignmentWithCourseBlock? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazzAssignment = db.onRepoWithFallbackToDb(2000){
            it.clazzAssignmentDao.findByUidWithBlockAsync(entityUid)
        } ?: ClazzAssignmentWithCourseBlock()

        loadAssignment(clazzAssignment, db)

        return clazzAssignment
    }

    private suspend fun loadAssignment(clazzAssignment: ClazzAssignmentWithCourseBlock, db: UmAppDatabase) {
        val loggedInPersonUid = accountManager.activeAccount.personUid

        val clazzWithSchool = db.onRepoWithFallbackToDb(2000) {
            it.clazzDao.getClazzWithSchool(clazzAssignment.caClazzUid)
        } ?: ClazzWithSchool()

        view.timeZone = clazzWithSchool.effectiveTimeZone()

        val clazzEnrolment: ClazzEnrolment? = db.onRepoWithFallbackToDb(2000) {
            it.clazzEnrolmentDao.findByPersonUidAndClazzUidAsync(loggedInPersonUid,
                    clazzAssignment.caClazzUid)
        }

        val isStudent = ClazzEnrolment.ROLE_STUDENT == (clazzEnrolment?.clazzEnrolmentRole ?: 0)
        view.showSubmission = isStudent

        if(isStudent) {

            val submitterUid = db.clazzAssignmentDao.getSubmitterUid(clazzAssignment.caUid,
                                                            accountManager.activeAccount.personUid)

            val unassignedMessage = systemImpl.getString(MessageID.unassigned, context)
            view.unassignedError = if(submitterUid == 0L) unassignedMessage else null

            checkCanAddFileOrText(clazzAssignment)

            // don't show private comments if unassigned in group
            view.showPrivateComments = clazzAssignment.caPrivateCommentsEnabled && submitterUid != 0L

            if(clazzAssignment.caPrivateCommentsEnabled){
                view.clazzAssignmentPrivateComments = db.commentsDao.findPrivateByEntityTypeAndUidAndForPersonLive2(
                    ClazzAssignment.TABLE_ID, clazzAssignment.caUid,
                    submitterUid)
            }

            view.submittedCourseAssignmentSubmission = db.onRepoWithFallbackToDb(2000){
                it.courseAssignmentSubmissionDao.getAllSubmissionsFromSubmitter(
                        clazzAssignment.caUid, submitterUid)
            }
            db.courseAssignmentSubmissionDao
                    .getStatusOfAssignmentForSubmitter(
                            clazzAssignment.caUid, submitterUid)
                    .observeWithLifecycleOwner(lifecycleOwner){
                        view.submissionStatus = it ?: 0
                    }

            db.courseAssignmentMarkDao.getMarkOfAssignmentForSubmitterLiveData(
                    clazzAssignment.caUid, submitterUid)
                    .observeWithLifecycleOwner(lifecycleOwner){
                        view.submissionMark = it
                    }
        }

        if(clazzAssignment.caClassCommentEnabled){
            view.clazzAssignmentClazzComments = db.commentsDao.findPublicByEntityTypeAndUidLive(
                    ClazzAssignment.TABLE_ID, clazzAssignment.caUid)
        }
    }

    suspend fun checkCanAddFileOrText(clazzAssignment: ClazzAssignmentWithCourseBlock) {
        val submitterUid = repo.clazzAssignmentDao.getSubmitterUid(clazzAssignment.caUid,
            accountManager.activeAccount.personUid)
        var alreadySubmitted = false
        if(clazzAssignment.caSubmissionPolicy == ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE){
            val sizeOfSubmitted = repo.courseAssignmentSubmissionDao.countSubmissionsFromSubmitter(
                clazzAssignment.caUid, submitterUid)
            alreadySubmitted = sizeOfSubmitted > 0
        }

        val hasPassedDeadline = hasPassedDeadline(clazzAssignment)
        val maxFilesReached = checkMaxFilesReached(db, clazzAssignment, submitterUid)
        val assignedInGroup = submitterUid != 0L

        view.addFileSubmissionVisible = clazzAssignment.caRequireFileSubmission
                && !alreadySubmitted && !maxFilesReached && !hasPassedDeadline && assignedInGroup


        view.addTextSubmissionVisible = clazzAssignment.caRequireTextSubmission
                && !alreadySubmitted && !hasPassedDeadline && assignedInGroup

    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzAssignmentWithCourseBlock? {
        super.onLoadFromJson(bundle)

        val entity = safeParse(di, ClazzAssignmentWithCourseBlock.serializer(), bundle[UstadEditView.ARG_ENTITY_JSON].toString())
        submissionList.addAll(
                safeParseList(di, ListSerializer(CourseAssignmentSubmissionWithAttachment.serializer()),
                CourseAssignmentSubmissionWithAttachment::class, bundle[SAVED_STATE_ADD_SUBMISSION_LIST]
                ?: ""))
        view.addedCourseAssignmentSubmission = submissionList

        presenterScope.launch {
            loadAssignment(entity, db)
        }

        return entity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        savedState.putEntityAsJson(UstadEditView.ARG_ENTITY_JSON, ClazzAssignment.serializer(), entity)
        savedState.putEntityAsJson(SAVED_STATE_ADD_SUBMISSION_LIST,
                ListSerializer(CourseAssignmentSubmissionWithAttachment.serializer()),
                submissionList)
    }

    private suspend fun checkMaxFilesReached(
        db: UmAppDatabase,
        clazzAssignment: ClazzAssignmentWithCourseBlock,
        submitterUid: Long
    ) : Boolean {
        val sizeOfAddedList = submissionList.filter { it.casType == CourseAssignmentSubmission.SUBMISSION_TYPE_FILE }.size
        val sizeOfSubmitted = db.courseAssignmentSubmissionDao.countFileSubmissionFromStudent(
            clazzAssignment.caUid, submitterUid)
        return (sizeOfAddedList + sizeOfSubmitted) >= clazzAssignment.caNumberOfFiles
    }

    private fun hasPassedDeadline(course: ClazzAssignmentWithCourseBlock): Boolean {
        val currentTime = systemTimeInMillis()
        return currentTime > (course.block?.cbGracePeriodDate ?: Long.MAX_VALUE)
    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()

        observeSavedStateResult(SAVED_STATE_KEY_URI, ListSerializer(String.serializer()),
                String::class) {
            val uri = it.firstOrNull() ?: return@observeSavedStateResult
            val entity = entity ?: return@observeSavedStateResult
            presenterScope.launch(doorMainDispatcher()) {
                val doorUri = DoorUri.parse(uri)

                val submitterUid = repo.clazzAssignmentDao.getSubmitterUid(entity.caUid,
                                                        accountManager.activeAccount.personUid)

                val submission = CourseAssignmentSubmissionWithAttachment().apply {
                    casSubmitterUid = submitterUid
                    casAssignmentUid = entity.caUid
                    casSubmitterPersonUid = accountManager.activeAccount.personUid
                    casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
                    casUid = db.doorPrimaryKeyManager.nextIdAsync(CourseAssignmentSubmission.TABLE_ID)
                }
                val attachment = CourseAssignmentSubmissionAttachment().apply {
                    casaUid =  db.doorPrimaryKeyManager.nextIdAsync(CourseAssignmentSubmissionAttachment.TABLE_ID)
                    casaSubmissionUid = submission.casUid
                    casaUri = uri
                    casaFileName = doorUri.getFileName(context)
                    casaMimeType = doorUri.guessMimeType(context, di)
                }
                submission.attachment = attachment
                submissionList.add(submission)
                view.addedCourseAssignmentSubmission = submissionList

                checkCanAddFileOrText(entity)

                requireSavedStateHandle()[SAVED_STATE_KEY_URI] = null
            }
        }

        observeSavedStateResult(SAVED_STATE_KEY_TEXT, ListSerializer(CourseAssignmentSubmissionWithAttachment.serializer()),
            CourseAssignmentSubmissionWithAttachment::class){
            val submission = it.firstOrNull() ?: return@observeSavedStateResult
            val entity = entity ?: return@observeSavedStateResult
            presenterScope.launch(doorMainDispatcher()) {

                val submitterUid = repo.clazzAssignmentDao.getSubmitterUid(entity.caUid,
                                                            accountManager.activeAccount.personUid)

                // find existing and remove it
                val existingSubmission = submissionList.find { subList -> subList.casUid == submission.casUid }
                submissionList.remove(existingSubmission)

                submission.casAssignmentUid = entity.caUid
                submission.casSubmitterUid =  submitterUid
                submission.casSubmitterPersonUid = accountManager.activeAccount.personUid

                submissionList.add(submission)
                view.addedCourseAssignmentSubmission = submissionList
                checkCanAddFileOrText(entity)

                requireSavedStateHandle()[SAVED_STATE_KEY_TEXT] = null
            }


        }

    }

    fun handleDeleteSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment) {
        submissionList.remove(submissionCourse)
        view.addedCourseAssignmentSubmission = submissionList
        presenterScope.launch {
            entity?.let {
                checkCanAddFileOrText(it)
            }
        }
    }

    fun handleEditSubmission(courseSubmission: CourseAssignmentSubmissionWithAttachment){
        if(courseSubmission.casType == CourseAssignmentSubmission.SUBMISSION_TYPE_TEXT){
            val args = mutableMapOf<String, String>()
            args.putEntityAsJson(UstadEditView.ARG_ENTITY_JSON,
                CourseAssignmentSubmissionWithAttachment.serializer(), courseSubmission)
            args[EDIT_ENABLED] = true.toString()

            navigateForResult(
                NavigateForResultOptions(
                    this@ClazzAssignmentDetailOverviewPresenter,
                    courseSubmission,
                    TextAssignmentEditView.VIEW_NAME,
                    CourseAssignmentSubmission::class,
                    CourseAssignmentSubmission.serializer(),
                    SAVED_STATE_KEY_TEXT,
                    arguments = args))
        }else{
            presenterScope.launch {
                openAssignmentFileAttachment(courseSubmission)
            }
        }
    }

    fun handleOpenSubmission(
        courseSubmission: CourseAssignmentSubmissionWithAttachment
    ){
        presenterScope.launch {
            if(courseSubmission.casType == CourseAssignmentSubmission.SUBMISSION_TYPE_TEXT){
                val args = mutableMapOf<String, String>()
                args[HtmlTextViewDetailView.DISPLAY_TEXT] = courseSubmission.casText ?: ""

                requireNavController().navigate(
                    HtmlTextViewDetailView.VIEW_NAME, args)

            }else if(courseSubmission.casType == CourseAssignmentSubmission.SUBMISSION_TYPE_FILE){
                openAssignmentFileAttachment(courseSubmission)
            }
        }
    }

    private suspend fun openAssignmentFileAttachment(courseSubmission: CourseAssignmentSubmissionWithAttachment){
        val fileSubmission = courseSubmission.attachment ?: return
        val uri = fileSubmission.casaUri ?: return
        val doorUri = if(uri.startsWith("door-attachment://")) repo.retrieveAttachment(uri) else DoorUri.parse(uri)
        try{
            systemImpl.openFileInDefaultViewer(context, doorUri, fileSubmission.casaMimeType, courseSubmission.attachment?.casaFileName)
        }catch (e: Exception){
            if (e is NoAppFoundException) {
                view.showSnackBar(systemImpl.getString(MessageID.no_app_found, context))
            }else{
                val message = e.message
                if (message != null) {
                    view.showSnackBar(message)
                }
            }
        }
    }

    fun handleSubmitButtonClicked(){
        presenterScope.launch(doorMainDispatcher()) {
            val entity = entity ?: return@launch
            val hasPassedDeadline = hasPassedDeadline(entity)
            if(hasPassedDeadline) {
                view.showSnackBar(systemImpl.getString(MessageID.deadline_has_passed, context))
                return@launch
            }

            val submitterUid = repo.clazzAssignmentDao.getSubmitterUid(entity.caUid,
                                                        accountManager.activeAccount.personUid)

            if(entity.caSubmissionPolicy == ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE){
                val sizeOfSubmitted = repo.courseAssignmentSubmissionDao
                    .countSubmissionsFromSubmitter(
                        entity.caUid,
                        submitterUid
                    )
                if(sizeOfSubmitted > 0){
                    view.showSnackBar(systemImpl.getString(MessageID.submission_already_made, context))
                    return@launch
                }
            }
            repo.withDoorTransactionAsync(UmAppDatabase::class) { txDb ->
                txDb.courseAssignmentSubmissionDao.insertListAsync(submissionList)
                txDb.courseAssignmentSubmissionAttachmentDao.insertListAsync(submissionList.mapNotNull { it.attachment })

                checkCanAddFileOrText(entity)

                // TODO need to handle groups
               /* val agentPerson = txDb.agentDao.getAgentFromPersonUsername(
                    accountManager.activeAccount.endpointUrl,
                    accountManager.activeAccount.username ?: ""
                ) ?: AgentEntity().apply {
                        agentPersonUid = accountManager.activeAccount.personUid
                        agentAccountName = accountManager.activeAccount.username
                        agentHomePage = accountManager.activeAccount.endpointUrl
                        agentUid = txDb.agentDao.insertAsync(this)
                }

                val submitStatement = StatementEntity().apply {
                    statementVerbUid = VerbEntity.VERB_SUBMITTED_UID
                    statementPersonUid = accountManager.activeAccount.personUid
                    statementClazzUid = entity?.caClazzUid ?: 0
                    xObjectUid = entity?.caXObjectUid ?: 0
                    agentUid = agentPerson.agentUid
                    contextRegistration = randomUuid().toString()
                    timestamp = systemTimeInMillis()
                    stored = systemTimeInMillis()
                    fullStatement = "" // TODO
                }
                txDb.statementDao.insertAsync(submitStatement)*/
            }

            submissionList.clear()
            view.addedCourseAssignmentSubmission = submissionList

        }
    }

    fun handleAddFileClicked(){
        val modeSelected: String = when(entity?.caFileType){
            ClazzAssignment.FILE_TYPE_DOC -> SelectFileView.SELECTION_MODE_DOC
            ClazzAssignment.FILE_TYPE_AUDIO -> SelectFileView.SELECTION_MODE_AUDIO
            ClazzAssignment.FILE_TYPE_VIDEO -> SelectFileView.SELECTION_MODE_VIDEO
            ClazzAssignment.FILE_TYPE_IMAGE -> SelectFileView.SELECTION_MODE_IMAGE
            else -> SelectFileView.SELECTION_MODE_ANY
        }

        val args = mutableMapOf(
                SelectFileView.ARG_MIMETYPE_SELECTED to modeSelected)

        navigateForResult(
                NavigateForResultOptions(this,
                        null, SelectFileView.VIEW_NAME, String::class,
                        String.serializer(), SAVED_STATE_KEY_URI,
                        arguments = args)
        )
    }

    fun handleAddTextClicked(){
        val args = mutableMapOf(TextAssignmentEditView.ASSIGNMENT_ID to entity?.caUid.toString())
        args[EDIT_ENABLED] = true.toString()
        navigateForResult(
                NavigateForResultOptions(this,
                        null,
                    TextAssignmentEditView.VIEW_NAME,
                    CourseAssignmentSubmission::class,
                    CourseAssignmentSubmission.serializer(),
                    SAVED_STATE_KEY_TEXT,
                    arguments = args))
    }


    companion object {

        @JvmStatic
        val SUBMISSION_POLICY_OPTIONS = mapOf(
            ClazzAssignment.SUBMISSION_POLICY_MULTIPLE_ALLOWED to MessageID.multiple_submission_allowed_submission_policy,
            ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE to MessageID.submit_all_at_once_submission_policy)

        const val SAVED_STATE_KEY_URI = "URI"

        const val SAVED_STATE_KEY_TEXT = "TEXT"

        const val SAVED_STATE_ADD_SUBMISSION_LIST = "submissionList"

        //TODO: Add constants for keys that would be used for any One To Many Join helpers
        const val  SAVEDSTATE_KEY_CLAZZ_ASSIGNMENT = "ClassAssignment"
    }

}