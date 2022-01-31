package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.storeMarkedStatement
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.SessionListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.attachments.retrieveAttachment
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.AssignmentFileSubmission
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentWithAttemptSummary
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class ClazzAssignmentDetailStudentProgressPresenter(context: Any, arguments: Map<String, String>, view: ClazzAssignmentDetailStudentProgressView,
                                                    di: DI, lifecycleOwner: DoorLifecycleOwner,
                                                    val newPrivateCommentListener: DefaultNewCommentItemListener =
                                                            DefaultNewCommentItemListener(di, context,
                                                                    arguments[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0L,
                                                                    ClazzAssignment.TABLE_ID, false,
                                                                    arguments[ARG_PERSON_UID]?.toLong() ?: 0L))
    : UstadDetailPresenter<ClazzAssignmentDetailStudentProgressView, ClazzAssignment>(context, arguments, view, di, lifecycleOwner),
        NewCommentItemListener by newPrivateCommentListener, ContentWithAttemptListener {


    val statementEndpoint by on(accountManager.activeAccount).instance<XapiStatementEndpoint>()

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return false
    }

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    var selectedPersonUid: Long = 0

    var selectedClazzAssignmentUid: Long= 0

    private var selectedClazzUid: Long = 0

    private var nextStudentToMark: Long = 0L

    override fun onCreate(savedState: Map<String, String>?) {
        selectedPersonUid = arguments[ARG_PERSON_UID]?.toLong() ?: 0
        selectedClazzAssignmentUid = arguments[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0
        selectedClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0
        super.onCreate(savedState)
        presenterScope.launch {
            repo.clazzAssignmentRollUpDao.cacheBestStatements(
                    selectedClazzUid, selectedClazzAssignmentUid,
                    selectedPersonUid)
            val clazzAssignmentObjectId = UMFileUtil.joinPaths(accountManager.activeAccount.endpointUrl,
                    "/clazzAssignment/${selectedClazzAssignmentUid}")
            nextStudentToMark = repo.clazzEnrolmentDao.findNextStudentNotMarkedForAssignment(clazzAssignmentObjectId,
                    selectedClazzAssignmentUid, selectedPersonUid)
            view.markNextStudentEnabled = nextStudentToMark != 0L
        }

    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignment? {
        val mLoggedInPersonUid = accountManager.activeAccount.personUid

        val clazzAssignment = db.onRepoWithFallbackToDb(2000) {
            db.clazzAssignmentDao.findByUidAsync(selectedClazzAssignmentUid)
        } ?: throw IllegalArgumentException("Clazz assignment uid not found")

        view.person = db.onRepoWithFallbackToDb(2000){
            it.personDao.findByUidAsync(selectedPersonUid)
        }

        view.clazzAssignmentContent =
                db.onRepoWithFallbackToDb(2000) {
                    it.clazzAssignmentContentJoinDao.findAllContentWithAttemptsByClazzAssignmentUid(
                            clazzAssignment.caUid, selectedPersonUid, mLoggedInPersonUid)
                }


        if(clazzAssignment.caRequireFileSubmission){

            view.clazzAssignmentFileSubmission = db.onRepoWithFallbackToDb(2000){
                it.assignmentFileSubmissionDao.getAllSubmittedFileSubmissionsFromStudent(
                        clazzAssignment.caUid,
                        selectedPersonUid
                )
            }

            db.clazzAssignmentRollUpDao.getScoreForFileSubmission(clazzAssignment.caUid, selectedPersonUid)
                    .observeWithLifecycleOwner(lifecycleOwner){
                        view.fileSubmissionScore = it
                    }
        }
        view.hasFileSubmission = clazzAssignment.caRequireFileSubmission

        db.clazzAssignmentDao.getStatementScoreProgressForAssignment(
                clazzAssignment.caUid, selectedPersonUid)
                .observeWithLifecycleOwner(lifecycleOwner){
                    view.studentScore = it
                }

        if(clazzAssignment.caPrivateCommentsEnabled){
            view.clazzAssignmentPrivateComments = db.commentsDao.findPrivateByEntityTypeAndUidAndForPersonLive2(
                    ClazzAssignment.TABLE_ID, clazzAssignment.caUid,
                    selectedPersonUid)
        }

        return clazzAssignment
    }

    fun onClickOpenFileSubmission(fileSubmission: AssignmentFileSubmission){
        presenterScope.launch {
            val uri = fileSubmission.afsUri ?: return@launch
            val doorUri = repo.retrieveAttachment(uri)
            try{
                systemImpl.openFileInDefaultViewer(context, doorUri, fileSubmission.afsMimeType)
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
    }


    fun onClickSubmitGrade(grade: Int): Boolean {
        if(grade < 0 || (grade > entity?.caMaxScore ?: 0)){
           // to highlight the textfield to show error
            view.submitMarkError = " "
            return false
        }
        val person = view.person ?: return false
        val assignment = view.entity ?: return false
        presenterScope.launch {
            val clazzAssignmentObjectId = UMFileUtil.joinPaths(accountManager.activeAccount.endpointUrl,
                    "/clazzAssignment/${selectedClazzAssignmentUid}")
            val statement = repo.statementDao.findSubmittedStatementFromStudent(person.personUid, clazzAssignmentObjectId)
            if(statement == null){
                view.submitMarkError = " "
                return@launch
            }
            withContext(Dispatchers.Default) {
                statementEndpoint.storeMarkedStatement(
                    accountManager.activeAccount,
                        person, randomUuid().toString(),
                        grade, assignment, statement)
                repo.clazzAssignmentRollUpDao.cacheBestStatements(selectedClazzUid, selectedClazzAssignmentUid, selectedPersonUid)
            }
        }
        return true
    }

    fun onClickSubmitGradeAndMarkNext(grade: Int) {
        val isValid = onClickSubmitGrade(grade)
        if(!isValid){
            return
        }
        systemImpl.go(ClazzAssignmentDetailStudentProgressView.VIEW_NAME,
                mapOf(ARG_PERSON_UID to nextStudentToMark.toString(),
                        ARG_CLAZZ_ASSIGNMENT_UID to selectedClazzAssignmentUid.toString(),
                        ARG_CLAZZUID to selectedClazzUid.toString()), context)
    }

    override fun onClickContentWithAttempt(contentWithAttemptSummary: ContentWithAttemptSummary) {
        val args =  mapOf(
                ARG_CONTENT_ENTRY_UID to contentWithAttemptSummary.contentEntryUid.toString(),
                ARG_PERSON_UID to selectedPersonUid.toString())
        systemImpl.go(SessionListView.VIEW_NAME, args, context)
    }

}