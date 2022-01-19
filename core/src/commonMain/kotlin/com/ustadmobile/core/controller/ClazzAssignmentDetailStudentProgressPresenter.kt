package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.SessionListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentWithAttemptSummary
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.launch
import org.kodein.di.DI

class ClazzAssignmentDetailStudentProgressPresenter(context: Any, arguments: Map<String, String>, view: ClazzAssignmentDetailStudentProgressView,
                                                    di: DI, lifecycleOwner: DoorLifecycleOwner,
                                                    val newPrivateCommentListener: DefaultNewCommentItemListener =
                                                            DefaultNewCommentItemListener(di, context,
                                                                    arguments[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0L,
                                                                    ClazzAssignment.TABLE_ID, false,
                                                                    arguments[ARG_PERSON_UID]?.toLong() ?: 0L))
    : UstadDetailPresenter<ClazzAssignmentDetailStudentProgressView, ClazzAssignment>(context, arguments, view, di, lifecycleOwner),
        NewCommentItemListener by newPrivateCommentListener, ContentWithAttemptListener {


    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return false
    }

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    var selectedPersonUid: Long = 0

    var selectedClazzAssignmentUid: Long= 0

    private var selectedClazzUid: Long = 0

    override fun onCreate(savedState: Map<String, String>?) {
        selectedPersonUid = arguments[ARG_PERSON_UID]?.toLong() ?: 0
        selectedClazzAssignmentUid = arguments[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0
        selectedClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0
        super.onCreate(savedState)
        presenterScope.launch {
            repo.clazzAssignmentRollUpDao.cacheBestStatements(
                    selectedClazzUid, selectedClazzAssignmentUid,
                    selectedPersonUid)
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

        }
        view.hasFileSubmission = clazzAssignment.caRequireFileSubmission

        view.studentScore = db.clazzAssignmentDao.getStatementScoreProgressForAssignment(
                clazzAssignment.caUid, selectedPersonUid)

        if(clazzAssignment.caPrivateCommentsEnabled){
            view.clazzAssignmentPrivateComments = db.commentsDao.findPrivateByEntityTypeAndUidAndForPersonLive2(
                    ClazzAssignment.TABLE_ID, clazzAssignment.caUid,
                    selectedPersonUid)
        }

        return clazzAssignment
    }

    fun onDownloadFileClicked(){

    }

    fun onClickSubmitGrade(){
        // TODO create statement with grade
    }

    fun onClickSubmitGradeAndMarkNext(){
        onClickSubmitGrade()
        // TODO navigate
    }

    override fun onClickContentWithAttempt(contentWithAttemptSummary: ContentWithAttemptSummary) {
        val args =  mapOf(
                ARG_CONTENT_ENTRY_UID to contentWithAttemptSummary.contentEntryUid.toString(),
                ARG_PERSON_UID to selectedPersonUid.toString())
        systemImpl.go(SessionListView.VIEW_NAME, args, context)
    }

}