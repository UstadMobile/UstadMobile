package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.util.ext.roundTo
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.HtmlTextViewDetailView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_SUBMITER_UID
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.attachments.retrieveAttachment
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class ClazzAssignmentDetailStudentProgressPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: ClazzAssignmentDetailStudentProgressView,
    di: DI,
    lifecycleOwner: DoorLifecycleOwner,
    // to enter the private comment to student/group
    val newPrivateCommentListener: DefaultNewCommentItemListener =
        DefaultNewCommentItemListener(
            di,
            context,
            arguments[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0L,
            ClazzAssignment.TABLE_ID,
            false,
            arguments[ARG_SUBMITER_UID]?.toLong() ?: 0L)
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

    var selectedSubmitterUid: Long = 0

    var selectedClazzAssignmentUid: Long= 0

    private var selectedClazzUid: Long = 0

    private var nextSubmitterToMark: Long = 0L

    override fun onCreate(savedState: Map<String, String>?) {
        selectedSubmitterUid = arguments[ARG_SUBMITER_UID]?.toLong() ?: 0
        selectedClazzAssignmentUid = arguments[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0
        selectedClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0
        super.onCreate(savedState)
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignmentWithCourseBlock? {
        val mLoggedInPersonUid = accountManager.activeAccount.personUid

        val clazzAssignment = db.onRepoWithFallbackToDb(2000) {
            db.clazzAssignmentDao.findByUidWithBlockAsync(selectedClazzAssignmentUid)
        } ?: throw IllegalArgumentException("Clazz assignment uid not found")

        val isGroup = clazzAssignment.caGroupUid != 0L

        view.submitterName = if(!isGroup){
            val person = db.onRepoWithFallbackToDb(2000){
                it.personDao.findByUidAsync(selectedSubmitterUid)
            }
            person?.personFullName()
        }else{
            systemImpl.getString(MessageID.group_number, context)
                .replace("%1\$s", "$selectedSubmitterUid")
        }

        view.clazzCourseAssignmentSubmissionAttachment = db.onRepoWithFallbackToDb(2000){
            it.courseAssignmentSubmissionDao.getAllSubmissionsFromSubmitter(
                    clazzAssignment.caUid, selectedSubmitterUid
            )
        }

        db.courseAssignmentMarkDao.getMarkOfAssignmentForSubmitterLiveData(
            clazzAssignment.caUid, selectedSubmitterUid)
            .observeWithLifecycleOwner(lifecycleOwner){
                        view.submissionScore = it
                    }

        db.courseAssignmentSubmissionDao
            .getStatusOfAssignmentForSubmitter(
                        clazzAssignment.caUid, selectedSubmitterUid)
            .observeWithLifecycleOwner(lifecycleOwner){
                    view.submissionStatus = it ?: 0
                }

        val submissionCount = repo.courseAssignmentSubmissionDao.countSubmissionsFromSubmitter(
            clazzAssignment.caUid, selectedSubmitterUid)
        val submitButtonVisible = submissionCount > 0
        view.submitButtonVisible = submitButtonVisible

        nextSubmitterToMark = repo.courseAssignmentMarkDao.findNextSubmitterToMarkForAssignment(
            selectedClazzAssignmentUid, selectedSubmitterUid)
        view.markNextStudentVisible = submitButtonVisible && nextSubmitterToMark != 0L


        if(clazzAssignment.caPrivateCommentsEnabled){
            view.clazzAssignmentPrivateComments = db.commentsDao.findPrivateByEntityTypeAndUidAndForPersonLive2(
                ClazzAssignment.TABLE_ID, clazzAssignment.caUid, selectedSubmitterUid
            )
        }

        return clazzAssignment
    }

    fun onClickOpenSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment){
        presenterScope.launch {
            if(submissionCourse.casType == CourseAssignmentSubmission.SUBMISSION_TYPE_TEXT){

                val args = mutableMapOf<String, String>()
                args[HtmlTextViewDetailView.DISPLAY_TEXT] = submissionCourse.casText ?: ""

                requireNavController().navigate(
                    HtmlTextViewDetailView.VIEW_NAME, args)

            }else if(submissionCourse.casType == CourseAssignmentSubmission.SUBMISSION_TYPE_FILE) {
                val attachment = submissionCourse.attachment ?: return@launch
                val uri = attachment.casaUri ?: return@launch
                val doorUri = repo.retrieveAttachment(uri)
                try {
                    systemImpl.openFileInDefaultViewer(context, doorUri, attachment.casaMimeType,
                        submissionCourse.attachment?.casaFileName)
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


    fun onClickSubmitGrade(grade: Float): Boolean {
        val maxPoints = entity?.block?.cbMaxPoints ?: 10
        if(grade < 0 || (grade > maxPoints)){
           // to highlight the textfield to show error
            view.submitMarkError = systemImpl.getString(
                MessageID.grade_out_of_range, context)
                .replace("%1\$s", maxPoints.toString())
            return false
        }

        val assignment = view.entity ?: return false
        presenterScope.launch {

            repo.withDoorTransactionAsync(UmAppDatabase::class) { txDb ->
                val lastSubmission = txDb.courseAssignmentSubmissionDao.findLastSubmissionFromStudent(
                    selectedSubmitterUid, assignment.caUid) ?: return@withDoorTransactionAsync

                val penalty = (assignment.block?.cbLateSubmissionPenalty ?: 0)
                val gradeAfterPenalty: Float = if(lastSubmission.casTimestamp > (assignment.block?.cbDeadlineDate ?: 0)){
                    val reducedGrade = grade * penalty / 100
                    (grade - reducedGrade).roundTo(2)
                }else{
                    grade.roundTo(2)
                }
                txDb.courseAssignmentMarkDao.insertAsync(CourseAssignmentMark().apply {
                    camSubmitterUid = selectedSubmitterUid
                    camAssignmentUid = assignment.caUid
                    camMark = gradeAfterPenalty
                    camPenalty = if(lastSubmission.casTimestamp > (assignment.block?.cbDeadlineDate ?: 0)) penalty else 0
                })

                view.showSnackBar(systemImpl.getString(MessageID.saved, context))

                // TODO needs to change to check for group instead of student
                /*  val statement = txDb.statementDao.findSubmittedStatementFromStudent(
                   selectedSubmitterUid, assignment.caXObjectUid)
               if(statement == null){
                   val message = "no submission statement for $selectedSubmitterUid for assignment $selectedClazzAssignmentUid"
                   txDb.errorReportDao.insertAsync(ErrorReport().apply {
                       errorCode = 404
                       severity = ErrorReport.SEVERITY_ERROR
                       this.message = message
                       osVersion = getOsVersion()
                       operatingSys = getOs()
                       timestamp = systemTimeInMillis()
                       presenterUri = ustadNavController?.currentBackStackEntry?.viewUri
                   })
                   Napier.e("Course Student Progress - $message")
                   return@withDoorTransactionAsync
               }
               // TODO get group username
               val agentPerson = txDb.agentDao.getAgentFromPersonUsername(
                   accountManager.activeAccount.endpointUrl, "")
                   ?: AgentEntity().apply {
                       agentPersonUid = accountManager.activeAccount.personUid
                       agentAccountName = ""
                       agentHomePage = accountManager.activeAccount.endpointUrl
                       agentUid = txDb.agentDao.insertAsync(this)
                   }

               val teacherAgent = txDb.agentDao.getAgentFromPersonUsername(
                   accountManager.activeAccount.endpointUrl, accountManager.activeAccount.username ?: "")
                   ?: AgentEntity().apply {
                       agentPersonUid = accountManager.activeAccount.personUid
                       agentAccountName = accountManager.activeAccount.username
                       agentHomePage = accountManager.activeAccount.endpointUrl
                       agentUid = txDb.agentDao.insertAsync(this)
                   }

               val statementRef = XObjectEntity().apply {
                   objectId = statement.statementId
                   objectType = "StatementRef"
                   objectStatementRefUid = statement.statementUid
                   xObjectUid = txDb.xObjectDao.insertAsync(this)
               }

               // check statementPersonUid for group
               val scoreStatement = StatementEntity().apply {
                   statementVerbUid = VerbEntity.VERB_SCORED_UID
                   statementPersonUid = selectedSubmitterUid
                   statementClazzUid = selectedClazzUid
                   xObjectUid = statementRef.xObjectUid
                   agentUid = agentPerson.agentUid
                   contextRegistration = randomUuid().toString()
                   instructorUid = teacherAgent.agentUid
                   resultCompletion = true
                   resultSuccess = StatementEntity.RESULT_SUCCESS
                   resultScoreRaw = gradeAfterPenalty.toLong()
                   resultScoreMax = maxPoints.toLong()
                   resultScoreScaled = (gradeAfterPenalty / (resultScoreMax))
                   timestamp = systemTimeInMillis()
                   stored = systemTimeInMillis()
                   fullStatement = "" // TODO
               }
               txDb.statementDao.insertAsync(scoreStatement)*/

            }

        }
        return true
    }

    fun onClickSubmitGradeAndMarkNext(grade: Float) {
        val isValid = onClickSubmitGrade(grade)
        if(!isValid){
            return
        }
        systemImpl.go(
            ClazzAssignmentDetailStudentProgressView.VIEW_NAME,
            mapOf(ARG_SUBMITER_UID to nextSubmitterToMark.toString(),
                        ARG_CLAZZ_ASSIGNMENT_UID to selectedClazzAssignmentUid.toString(),
                        ARG_CLAZZUID to selectedClazzUid.toString()),
            context,
            UstadMobileSystemCommon.UstadGoOptions(CURRENT_DEST, true))
    }

}