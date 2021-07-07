package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

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

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        selectedPersonUid = arguments[ARG_PERSON_UID]?.toLong() ?: 0
        selectedClazzAssignmentUid = arguments[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignment? {
        val mLoggedInPersonUid = accountManager.activeAccount.personUid

        val clazzAssignment = db.onRepoWithFallbackToDb(2000) {
            db.clazzAssignmentDao.findByUidAsync(selectedClazzAssignmentUid)
        } ?: ClazzAssignment().apply {
            caUid = selectedClazzAssignmentUid
        }

        view.person = db.onRepoWithFallbackToDb(2000){
            it.personDao.findByUidAsync(selectedPersonUid)
        }

        view.clazzAssignmentContent =
                db.onRepoWithFallbackToDb(2000) {
                    it.clazzAssignmentContentJoinDao.findAllContentWithAttemptsByClazzAssignmentUid(
                            clazzAssignment.caUid, selectedPersonUid, mLoggedInPersonUid)
                }

        view.studentScore = db.clazzAssignmentDao.getStatementScoreProgressForAssignment(
                clazzAssignment.caUid, selectedPersonUid)

        if(clazzAssignment.caPrivateCommentsEnabled){
            view.clazzAssignmentPrivateComments = db.commentsDao.findPrivateByEntityTypeAndUidAndForPersonLive2(
                    ClazzAssignment.TABLE_ID, clazzAssignment.caUid,
                    selectedPersonUid)
        }

        return clazzAssignment
    }

    override fun onClickContentWithAttempt(contentWithAttemptSummary: ContentWithAttemptSummary) {
        val args =  mapOf(
                ARG_CONTENT_ENTRY_UID to contentWithAttemptSummary.contentEntryUid.toString(),
                ARG_PERSON_UID to selectedPersonUid.toString())
        systemImpl.go(SessionListView.VIEW_NAME, args, context)
    }

}