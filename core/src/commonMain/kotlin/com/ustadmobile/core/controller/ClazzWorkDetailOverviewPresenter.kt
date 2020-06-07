package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.findClazzTimeZone
import com.ustadmobile.core.view.ClazzWorkEditView
import com.ustadmobile.core.view.ClazzWorkDetailOverviewView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull


class ClazzWorkDetailOverviewPresenter(context: Any,
                                       arguments: Map<String, String>, view: ClazzWorkDetailOverviewView,
                                       lifecycleOwner: DoorLifecycleOwner,
                                       systemImpl: UstadMobileSystemImpl,
                                       db: UmAppDatabase, repo: UmAppDatabase,
                                       activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadDetailPresenter<ClazzWorkDetailOverviewView, ClazzWorkWithSubmission>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {


    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWorkWithSubmission? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val loggedInPersonUid = UmAccountManager.getActivePersonUid(context)

        val clazzWorkWithSubmission = withTimeoutOrNull(2000){
            db.clazzWorkDao.findWithSubmissionByUidAndPerson(entityUid, loggedInPersonUid)
        }?: ClazzWorkWithSubmission()


        val clazzWithSchool = withTimeoutOrNull(2000){
            db.clazzDao.getClazzWithSchool(clazzWorkWithSubmission.clazzWorkClazzUid)
        }?: ClazzWithSchool()

        view.timeZone = clazzWithSchool.findClazzTimeZone()

        //Find Content and questions
        val contentList = withTimeoutOrNull(2000) {
            db.clazzWorkContentJoinDao.findContentByClazzWorkUidLive(
                    clazzWorkWithSubmission.clazzWorkUid,
                    clazzWorkWithSubmission.clazzWorkStartDateTime,
                    clazzWorkWithSubmission.clazzWorkDueDateTime)
        }

        view.clazzWorkContent = contentList

        val clazzMember: ClazzMember? = withTimeoutOrNull(2000){
            db.clazzMemberDao.findByPersonUidAndClazzUid(loggedInPersonUid, entity?.clazzWorkClazzUid?:0L)
        }

        //TODO: DIsable AFTER TESTING
        //view.studentMode = (clazzMember != null && clazzMember.clazzMemberRole == ClazzMember.ROLE_STUDENT)
        view.studentMode = true


        val questionAndOptions: List<ClazzWorkQuestionAndOptionRow> =
                withTimeoutOrNull(2000) {
                    db.clazzWorkQuestionDao.findAllActiveQuestionsWithOptionsInClazzWorkAsList(entityUid)
                }?: listOf()

        val questionsAndOptionsWithResponseList: List<ClazzWorkQuestionAndOptionWithResponse> =
                questionAndOptions.groupBy { it.clazzWorkQuestion }.entries
                        .map {
                            val questionUid = it.key?.clazzWorkQuestionUid?:0L
                            val qResponse = db.clazzWorkQuestionResponseDao.findByQuestionUidAsync(
                                    questionUid).toMutableList()
                            if(qResponse.isEmpty()){
                                qResponse.add(ClazzWorkQuestionResponse().apply {
                                    clazzWorkQuestionResponseQuestionUid = questionUid
                                    clazzWorkQuestionResponsePersonUid = loggedInPersonUid
                                    clazzWorkQuestionResponseClazzMemberUid = clazzMember?.clazzMemberUid?:0L
                                    clazzWorkQuestionResponseClazzWorkUid = entity?.clazzWorkUid?:0L

                                })
                            }
                            ClazzWorkQuestionAndOptionWithResponse(
                                    entity?:ClazzWorkWithSubmission(),
                                it.key?: ClazzWorkQuestion(),
                                it.value.map { it.clazzWorkQuestionOption?: ClazzWorkQuestionOption() },
                                    qResponse.first()) }

        view.clazzWorkQuizQuestionsAndOptionsWithResponse = DoorMutableLiveData(questionsAndOptionsWithResponseList)


        val publicComments = withTimeoutOrNull(2000){
            db.commentsDao.findPublicByEntityTypeAndUidLive(ClazzWork.CLAZZ_WORK_TABLE_ID,
                    clazzWorkWithSubmission.clazzWorkUid)
        }
        view.clazzWorkPublicComments = publicComments


        val privateComments = withTimeoutOrNull(2000){
            db.commentsDao.findPrivateByEntityTypeAndUidLive(ClazzWork.CLAZZ_WORK_TABLE_ID,
                    clazzWorkWithSubmission.clazzWorkUid)
        }
        view.clazzWorkPrivateComments = privateComments


        return clazzWorkWithSubmission
    }

    override fun handleClickEdit() {
        systemImpl.go(ClazzWorkEditView.VIEW_NAME , arguments, context)
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        //TODO: this
        return true
    }

    fun handleClickSubmit(){
        print("Hi")

        val questionsWithOptionsAndResponse = view.clazzWorkQuizQuestionsAndOptionsWithResponse?.getValue()?: listOf()
        val newOptionsAndResponse = mutableListOf<ClazzWorkQuestionAndOptionWithResponse>()

        val clazzWorkWithSubmission = entity
        GlobalScope.launch {
            for (everyResult in questionsWithOptionsAndResponse) {
                val response = everyResult.clazzWorkQuestionResponse
                if(response.clazzWorkQuestionResponseUid == 0L) {
                    response.clazzWorkQuestionResponseUid = db.clazzWorkQuestionResponseDao.insertAsync(response)
                }else{
                    db.clazzWorkQuestionResponseDao.updateAsync(response)
                }
                everyResult.clazzWorkQuestionResponse = response
                newOptionsAndResponse.add(everyResult)
            }

            val loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
            val clazzMember: ClazzMember? = withTimeoutOrNull(2000){
                db.clazzMemberDao.findByPersonUidAndClazzUid(loggedInPersonUid, entity?.clazzWorkClazzUid?:0L)
            }

            var submission = entity?.clazzWorkSubmission
            if(submission == null) {
                submission = ClazzWorkSubmission().apply {
                    clazzWorkSubmissionClazzWorkUid = clazzWorkWithSubmission?.clazzWorkUid ?: 0L
                    clazzWorkSubmissionClazzMemberUid = clazzMember?.clazzMemberUid ?: 0L
                    clazzWorkSubmissionDateTimeFinished = UMCalendarUtil.getDateInMilliPlusDays(0)
                    clazzWorkSubmissionInactive = false
                    clazzWorkSubmissionPersonUid = loggedInPersonUid
                }
            }
            if(submission.clazzWorkSubmissionUid == 0L) {
                submission.clazzWorkSubmissionUid = db.clazzWorkSubmissionDao.insertAsync(submission)
            }else{
                db.clazzWorkSubmissionDao.updateAsync(submission)
            }
            clazzWorkWithSubmission?.clazzWorkSubmission = submission
            view.entity = clazzWorkWithSubmission

            //TODO : Update View
            view.runOnUiThread(Runnable {
                view.clazzWorkQuizQuestionsAndOptionsWithResponse = DoorMutableLiveData(newOptionsAndResponse)
            })

        }
    }

    fun addComment(comment: String, commentPublic: Boolean){
        val comment = Comments(ClazzWork.CLAZZ_WORK_TABLE_ID, entity?.clazzWorkUid?:0L,
            UmAccountManager.getActivePersonUid(context), UMCalendarUtil.getDateInMilliPlusDays(0),
        comment, commentPublic)
        GlobalScope.launch {
            db.commentsDao.insertAsync(comment)
        }
    }

    fun updateQuestionResponseAndView(questionUid: Long, responseLong: Long, responseString: String){



    }



}