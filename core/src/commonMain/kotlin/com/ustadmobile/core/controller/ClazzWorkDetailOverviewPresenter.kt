package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.getQuestionListForView
import com.ustadmobile.core.view.ClazzWorkDetailOverviewView
import com.ustadmobile.core.view.ClazzWorkEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class ClazzWorkDetailOverviewPresenter(context: Any,
                                       arguments: Map<String, String>, view: ClazzWorkDetailOverviewView,
                                       di: DI, lifecycleOwner: DoorLifecycleOwner,
                                       private val newCommentItemListener: DefaultNewCommentItemListener =
                                               DefaultNewCommentItemListener(di, context)
)
    : UstadDetailPresenter<ClazzWorkDetailOverviewView, ClazzWorkWithSubmission>(context,
        arguments, view, di, lifecycleOwner)
        , NewCommentItemListener by newCommentItemListener {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWorkWithSubmission? {
        val clazzWorkUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val loggedInPersonUid = accountManager.activeAccount.personUid

        val clazzWorkWithSubmission = withTimeoutOrNull(2000){
            db.clazzWorkDao.findWithSubmissionByUidAndPerson(clazzWorkUid, loggedInPersonUid)
        }?: ClazzWorkWithSubmission()


        val clazzWithSchool = withTimeoutOrNull(2000) {
            db.clazzDao.getClazzWithSchool(clazzWorkWithSubmission.clazzWorkClazzUid)
        } ?: ClazzWithSchool()

        view.timeZone = clazzWithSchool.effectiveTimeZone()

        val loggedInPerson = withTimeoutOrNull(2000){
            db.personDao.findByUidAsync(loggedInPersonUid)
        }
        val clazzEnrolment: ClazzEnrolment? = withTimeoutOrNull(2000){
            db.clazzEnrolmentDao.findByPersonUidAndClazzUidAsync(loggedInPersonUid,
                    clazzWorkWithSubmission.clazzWorkClazzUid)
        }

        if(loggedInPerson?.admin == true){
            view.isStudent = false
        }else{
            if(clazzEnrolment == null){
                view.isStudent = false
            }else {
                view.isStudent = (clazzEnrolment.clazzEnrolmentRole != ClazzEnrolment.ROLE_TEACHER)
            }
        }

        //If Submission object doesn't exist, create it.
        if(clazzWorkWithSubmission.clazzWorkSubmission == null && view.isStudent){
            clazzWorkWithSubmission.clazzWorkSubmission = ClazzWorkSubmission().apply {
                clazzWorkSubmissionClazzWorkUid = clazzWorkWithSubmission.clazzWorkUid
                clazzWorkSubmissionClazzEnrolmentUid = clazzEnrolment?.clazzEnrolmentUid?:0L
                clazzWorkSubmissionPersonUid = loggedInPersonUid
                clazzWorkSubmissionInactive = false
                clazzWorkSubmissionDateTimeStarted = getSystemTimeInMillis()
            }
        }

        if(clazzWorkWithSubmission.clazzWorkSubmissionType == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ) {

            val questionsAndOptionsWithResponseList = db.getQuestionListForView(clazzWorkWithSubmission,
                    clazzEnrolment?.clazzEnrolmentUid?:0L,loggedInPersonUid )

            if(view.isStudent && clazzWorkWithSubmission.clazzWorkSubmission?.clazzWorkSubmissionUid == 0L ) {
                view.editableQuizQuestions =
                        DoorMutableLiveData(questionsAndOptionsWithResponseList)
            }else{
                view.viewOnlyQuizQuestions = DoorMutableLiveData(questionsAndOptionsWithResponseList)
            }
        }

        newCommentItemListener.fromPerson = loggedInPersonUid
        newCommentItemListener.entityId = clazzWorkUid

        //Find Content and questions
        view.clazzWorkContent =
                withTimeoutOrNull(2000) {
                    repo.clazzWorkContentJoinDao.findAllContentByClazzWorkUidDF(
                            clazzWorkUid, loggedInPersonUid)
                }


        view.clazzWorkPublicComments = repo.commentsDao.findPublicByEntityTypeAndUidLive(
                ClazzWork.CLAZZ_WORK_TABLE_ID, clazzWorkWithSubmission.clazzWorkUid)

        if(view.isStudent) {
            view.clazzWorkPrivateComments = repo.commentsDao.findPrivateByEntityTypeAndUidAndForPersonLive2(
                            ClazzWork.CLAZZ_WORK_TABLE_ID, clazzWorkWithSubmission.clazzWorkUid,
                            loggedInPersonUid)
            view.showMarking = clazzWorkWithSubmission.clazzWorkSubmission?.
                        clazzWorkSubmissionMarkerPersonUid != 0L

            if(clazzWorkWithSubmission.clazzWorkSubmissionType ==
                    ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT){
                view.showFreeTextSubmission = true
            }

            if(clazzWorkWithSubmission.clazzWorkSubmissionType ==
                    ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ){
                view.showQuestionHeading = true
            }

            if((clazzWorkWithSubmission.clazzWorkSubmission?.clazzWorkSubmissionUid == 0L
                            || clazzWorkWithSubmission.clazzWorkSubmission == null)
                    &&
                    (clazzWorkWithSubmission.clazzWorkSubmission == null
                            || clazzWorkWithSubmission.clazzWorkSubmissionType !=
                            ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE)){
                view.showSubmissionButton = true
            }

            if(!view.showMarking || !view.showFreeTextSubmission || !view.showSubmissionButton){
                view.showSubmissionHeading = true
            }
        }

        if(!clazzWorkWithSubmission.clazzWorkCommentsEnabled && view.isStudent){
            view.showPrivateComments = true
            view.showNewPrivateComment = false

        }else if (view.isStudent){
            view.showPrivateComments = true
            view.showNewPrivateComment = true
        }else{
            view.showPrivateComments = false
            view.showNewPrivateComment = false
        }

        return clazzWorkWithSubmission
    }

    override fun handleClickEdit() {
        systemImpl.go(ClazzWorkEditView.VIEW_NAME , arguments, context)
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        val clazzUid = withTimeoutOrNull(2000) {
            repo.clazzWorkDao.findByUidAsync(arguments[ARG_ENTITY_UID]?.toLong()
                    ?: 0)?.clazzWorkClazzUid
        } ?: 0L

        return db.clazzDao.personHasPermissionWithClazz(accountManager.activeAccount.personUid,
                    clazzUid, Role.PERMISSION_CLAZZWORK_UPDATE)
    }

    fun handleClickSubmit(){


        val questionsWithOptionsAndResponse =
                view.editableQuizQuestions?.getValue()?: listOf()
        val newOptionsAndResponse = mutableListOf<ClazzWorkQuestionAndOptionWithResponse>()

        view.editableQuizQuestions = DoorMutableLiveData(listOf())
        view.viewOnlyQuizQuestions = DoorMutableLiveData(listOf())

        val clazzWorkWithSubmission = entity
        GlobalScope.launch {
            for (everyResult in questionsWithOptionsAndResponse) {
                val response = everyResult.clazzWorkQuestionResponse
                if(response.clazzWorkQuestionResponseUid == 0L) {
                    response.clazzWorkQuestionResponseUid =
                            repo.clazzWorkQuestionResponseDao.insertAsync(response)
                }else{
                    repo.clazzWorkQuestionResponseDao.updateAsync(response)
                }
                everyResult.clazzWorkQuestionResponse = response
                newOptionsAndResponse.add(everyResult)
            }

            val loggedInPersonUid = accountManager.activeAccount.personUid
            val clazzEnrolment: ClazzEnrolment? = withTimeoutOrNull(2000){
                repo.clazzEnrolmentDao.findByPersonUidAndClazzUidAsync(loggedInPersonUid,
                        entity?.clazzWorkClazzUid?:0L)
            }

            val submission = entity?.clazzWorkSubmission ?: ClazzWorkSubmission().apply {
                clazzWorkSubmissionClazzWorkUid = clazzWorkWithSubmission?.clazzWorkUid ?: 0L
                clazzWorkSubmissionClazzEnrolmentUid = clazzEnrolment?.clazzEnrolmentUid ?: 0L
                clazzWorkSubmissionInactive = false
                clazzWorkSubmissionPersonUid = loggedInPersonUid
            }

            submission.clazzWorkSubmissionDateTimeFinished = getSystemTimeInMillis()

            if(submission.clazzWorkSubmissionUid == 0L) {
                submission.clazzWorkSubmissionUid = repo.clazzWorkSubmissionDao.insertAsync(submission)
            }else{
                repo.clazzWorkSubmissionDao.updateAsync(submission)
            }
            clazzWorkWithSubmission?.clazzWorkSubmission = submission
            view.runOnUiThread(Runnable {
                view.entity = clazzWorkWithSubmission
                view.viewOnlyQuizQuestions =
                        DoorMutableLiveData(newOptionsAndResponse)
            })

        }
    }


}