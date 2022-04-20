package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.storeMarkedStatement
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.HtmlTextViewDetailView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.attachments.retrieveAttachment
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class ClazzAssignmentDetailStudentProgressPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: ClazzAssignmentDetailStudentProgressView,
    di: DI,
    lifecycleOwner: DoorLifecycleOwner,
    val newPrivateCommentListener: DefaultNewCommentItemListener =
        DefaultNewCommentItemListener(
            di,
            context,
            arguments[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0L,
            ClazzAssignment.TABLE_ID,
            false,
            arguments[ARG_PERSON_UID]?.toLong() ?: 0L)
) : UstadDetailPresenter<ClazzAssignmentDetailStudentProgressView, ClazzAssignmentWithCourseBlock>(
    context,
    arguments,
    view,
    di,
    lifecycleOwner
), NewCommentItemListener by newPrivateCommentListener {


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
            nextStudentToMark = repo.courseAssignmentMarkDao.findNextStudentToMarkForAssignment(
                    selectedClazzAssignmentUid, selectedPersonUid)
            view.markNextStudentEnabled = nextStudentToMark != 0L
        }

    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignmentWithCourseBlock? {
        val mLoggedInPersonUid = accountManager.activeAccount.personUid

        val clazzAssignment = db.onRepoWithFallbackToDb(2000) {
            db.clazzAssignmentDao.findByUidWithBlockAsync(selectedClazzAssignmentUid)
        } ?: throw IllegalArgumentException("Clazz assignment uid not found")

        view.person = db.onRepoWithFallbackToDb(2000){
            it.personDao.findByUidAsync(selectedPersonUid)
        }


        view.clazzCourseAssignmentSubmissionAttachment = db.onRepoWithFallbackToDb(2000){
            it.courseAssignmentSubmissionDao.getAllFileSubmissionsFromStudent(
                    clazzAssignment.caUid,
                    selectedPersonUid
            )
        }

        db.courseAssignmentMarkDao.getMarkOfAssignmentForStudent(clazzAssignment.caUid, selectedPersonUid)
                    .observeWithLifecycleOwner(lifecycleOwner){
                        view.submissionScore = it
                    }

        db.courseAssignmentSubmissionDao
                .getStatusOfAssignmentForStudent(
                        clazzAssignment.caUid, selectedPersonUid)
                .observeWithLifecycleOwner(lifecycleOwner){
                    view.submissionStatus = it ?: 0
                }


        if(clazzAssignment.caPrivateCommentsEnabled){
            view.clazzAssignmentPrivateComments = db.commentsDao.findPrivateByEntityTypeAndUidAndForPersonLive2(
                    ClazzAssignment.TABLE_ID, clazzAssignment.caUid,
                    selectedPersonUid)
        }

        return clazzAssignment
    }

    fun onClickOpenSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment){
        presenterScope.launch {
            if(submissionCourse.casType == CourseAssignmentSubmission.SUBMISSION_TYPE_TEXT){

                val args = mutableMapOf<String, String>()
                args[HtmlTextViewDetailView.DISPLAY_TEXT] = submissionCourse.casText ?: ""

                ustadNavController.navigate(
                    HtmlTextViewDetailView.VIEW_NAME, args)

            }else if(submissionCourse.casType == CourseAssignmentSubmission.SUBMISSION_TYPE_FILE) {
                val attachment = submissionCourse.attachment ?: return@launch
                val uri = attachment.casaUri ?: return@launch
                val doorUri = repo.retrieveAttachment(uri)
                try {
                    systemImpl.openFileInDefaultViewer(context, doorUri, attachment.casaMimeType)
                } catch (e: Exception) {
                    if (e is NoAppFoundException) {
                        view.showSnackBar(systemImpl.getString(MessageID.no_app_found, context))
                    } else {
                        val message = e.message
                        if (message != null) {
                            view.showSnackBar(message)
                        }
                    }
                }
            }
        }
    }


    fun onClickSubmitGrade(grade: Int): Boolean {
        if(grade < 0 || (grade > (entity?.block?.cbMaxPoints ?: 0))){
           // to highlight the textfield to show error
            view.submitMarkError = " "
            return false
        }
        val person = view.person ?: return false
        val assignment = view.entity ?: return false
        presenterScope.launch {
            val statement = repo.statementDao.findSubmittedStatementFromStudent(
                person.personUid, assignment.caXObjectUid)
            if(statement == null){
                view.submitMarkError = " "
                return@launch
            }
            db.courseAssignmentMarkDao.insertAsync(CourseAssignmentMark().apply {
                camStudentUid = person.personUid
                camAssignmentUid = assignment.caUid
                camMark = grade
                camPenalty = if(statement.timestamp > (assignment.block?.cbDeadlineDate ?: 0)) assignment.block?.cbLateSubmissionPenalty ?: 0 else 0
            })
            withContext(Dispatchers.Default) {
                statementEndpoint.storeMarkedStatement(
                    accountManager.activeAccount,
                        person, randomUuid().toString(),
                        grade, assignment, statement)
                view.showSnackBar(systemImpl.getString(MessageID.saved, context))
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

}