package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.SelectFileView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.kodein.di.DI


class ClazzAssignmentDetailOverviewPresenter(context: Any,
                                             arguments: Map<String, String>, view: ClazzAssignmentDetailOverviewView,
                                             lifecycleOwner: DoorLifecycleOwner,
                                             di: DI,val newPrivateCommentListener: DefaultNewCommentItemListener =
                                                     DefaultNewCommentItemListener(di, context,
                                                             arguments[ARG_ENTITY_UID]?.toLong() ?: 0L,
                                                     ClazzAssignment.TABLE_ID, false),
                                             val newClassCommentListener: DefaultNewCommentItemListener =
                                                     DefaultNewCommentItemListener(di, context,
                                                             arguments[ARG_ENTITY_UID]?.toLong() ?: 0L,
                                                             ClazzAssignment.TABLE_ID, true))
    : UstadDetailPresenter<ClazzAssignmentDetailOverviewView, ClazzAssignment>(context, arguments, view, di, lifecycleOwner){


    override val persistenceMode: PersistenceMode
          get() = PersistenceMode.DB

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        val clazzAssignment = withTimeoutOrNull(2000) {
            repo.clazzAssignmentDao.findByUidAsync(arguments[ARG_ENTITY_UID]?.toLong()
                    ?: 0)
        }

        return db.clazzDao.personHasPermissionWithClazz(accountManager.activeAccount.personUid,
                clazzAssignment?.caClazzUid ?: 0, Role.PERMISSION_ASSIGNMENT_UPDATE)
    }

    override fun handleClickEdit() {
        systemImpl.go(ClazzAssignmentEditView.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to entity?.caUid.toString()), context)
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignment? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val loggedInPersonUid = accountManager.activeAccount.personUid


        val clazzAssignment = db.onRepoWithFallbackToDb(2000){
            it.clazzAssignmentDao.findByUidAsync(entityUid)
        } ?: ClazzAssignment()


        val clazzWithSchool = db.onRepoWithFallbackToDb(2000) {
            it.clazzDao.getClazzWithSchool(clazzAssignment.caClazzUid)
        } ?: ClazzWithSchool()

        view.timeZone = clazzWithSchool.effectiveTimeZone()

        view.clazzAssignmentContent =
                db.onRepoWithFallbackToDb(2000) {
                    it.clazzAssignmentContentJoinDao.findAllContentByClazzAssignmentUidDF(
                            clazzAssignment.caUid, loggedInPersonUid)
                }

        val clazzEnrolment: ClazzEnrolment? = db.onRepoWithFallbackToDb(2000) {
            it.clazzEnrolmentDao.findByPersonUidAndClazzUidAsync(loggedInPersonUid,
                    clazzAssignment.caClazzUid)
        }

        val isStudent = ClazzEnrolment.ROLE_STUDENT == clazzEnrolment?.clazzEnrolmentRole ?: 0

        if(isStudent) {
            view.showFileSubmission = clazzAssignment.caRequireFileSubmission
            view.maxNumberOfFilesSubmission = clazzAssignment.caNumberOfFiles
            if(clazzAssignment.caRequireFileSubmission) {
                val currentTime = systemTimeInMillis()
                view.hasPassedDeadline = when (clazzAssignment.caEditAfterSubmissionType) {
                    ClazzAssignment.EDIT_AFTER_SUBMISSION_TYPE_ALLOWED_DEADLINE -> {
                        currentTime > clazzAssignment.caDeadlineDate
                    }
                    ClazzAssignment.EDIT_AFTER_SUBMISSION_TYPE_ALLOWED_GRACE -> {
                        currentTime > clazzAssignment.caGracePeriodDate
                    }
                    else -> currentTime > clazzAssignment.caDeadlineDate
                }

                view.clazzAssignmentFileSubmission = db.onRepoWithFallbackToDb(2000){
                    it.assignmentFileSubmissionDao.getAllFileSubmissionsFromStudent(
                            clazzAssignment.caUid, loggedInPersonUid)
                }


            }
            view.clazzMetrics = db.clazzAssignmentDao.getStatementScoreProgressForAssignment(
                    clazzAssignment.caUid, loggedInPersonUid)
        }else{
            // isTeacher
            view.showFileSubmission = false
        }


        if(isStudent && clazzAssignment.caPrivateCommentsEnabled){
            view.clazzAssignmentPrivateComments = db.commentsDao.findPrivateByEntityTypeAndUidAndForPersonLive2(
                    ClazzAssignment.TABLE_ID, clazzAssignment.caUid,
                    loggedInPersonUid)
            view.showPrivateComments = true
        }else{
            view.showPrivateComments = false
        }

        if(clazzAssignment.caClassCommentEnabled){
            view.clazzAssignmentClazzComments = db.commentsDao.findPublicByEntityTypeAndUidLive(
                    ClazzAssignment.TABLE_ID, clazzAssignment.caUid)
        }

        return clazzAssignment
    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()

        observeSavedStateResult(SAVED_STATE_KEY_URI, ListSerializer(String.serializer()),
                String::class) {
            val uri = it.firstOrNull() ?: return@observeSavedStateResult
            presenterScope.launch(doorMainDispatcher()) {
                repo.assignmentFileSubmissionDao.insertAsync(AssignmentFileSubmission().apply {
                    afsAssignmentUid = entity?.caUid ?: 0
                    afsStudentUid = accountManager.activeAccount.personUid
                    afsTitle = DoorUri.parse(uri).getFileName(context)
                    afsUri = uri
                })
            }
            requireSavedStateHandle()[ContentEntryEdit2Presenter.SAVED_STATE_KEY_URI] = null
        }

    }

    fun handleDeleteFileSubmission(fileSubmission: AssignmentFileSubmission) {
        presenterScope.launch {
            fileSubmission.afsActive = false
            fileSubmission.afsUri = null
            repo.assignmentFileSubmissionDao.updateAsync(fileSubmission)
        }
    }

    fun handleSubmitButtonClicked(){
        presenterScope.launch {
            repo.assignmentFileSubmissionDao.setFilesAsSubmittedForStudent(
                    entity?.caUid ?: 0,
                    accountManager.activeAccount.personUid, true, systemTimeInMillis())
        }
    }

    fun handleAddFileClicked(){
        val modeSelected: String = when(entity?.caFileType){
            ClazzAssignment.FILE_TYPE_DOC -> SelectFileView.SELECTION_MODE_FILE
            ClazzAssignment.FILE_TYPE_AUDIO -> SelectFileView.SELECTION_MODE_AUDIO
            ClazzAssignment.FILE_TYPE_VIDEO -> SelectFileView.SELECTION_MODE_VIDEO
            ClazzAssignment.FILE_TYPE_IMAGE -> SelectFileView.SELECTION_MODE_IMAGE
            else -> SelectFileView.SELECTION_MODE_ANY
        }

        val args = mutableMapOf(
                SelectFileView.ARG_SELECTION_MODE to modeSelected)

        navigateForResult(
                NavigateForResultOptions(this,
                        null, SelectFileView.VIEW_NAME, String::class,
                        String.serializer(), SAVED_STATE_KEY_URI,
                        arguments = args)
        )
    }


    companion object {

        const val SAVED_STATE_KEY_URI = "URI"

    }

}