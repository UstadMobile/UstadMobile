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

    var clazzWorkWithSubmissionLiveData: DoorMutableLiveData<List<ClazzWorkWithSubmission>>? = null

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

        view.studentMode = false

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
                                    clazzWorkQuestionResponsePersonUid = 0L
                                    clazzWorkQuestionResponseClazzMemberUid = 0L

                                })
                            }
                            ClazzWorkQuestionAndOptionWithResponse(
                                it.key?: ClazzWorkQuestion(),
                                it.value.map { it.clazzWorkQuestionOption?: ClazzWorkQuestionOption() },
                                    qResponse.first()) }

        view.clazzWorkQuizQuestionsAndOptionsWithResponse?.sendValue(questionsAndOptionsWithResponseList)


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

    fun handleClickSave(entity: ClazzWorkWithSubmission){
        print(entity.clazzWorkSubmission?.clazzWorkSubmissionText)
    }

    fun addComment(comment: String, commentPublic: Boolean){
        val comment = Comments(ClazzWork.CLAZZ_WORK_TABLE_ID, entity?.clazzWorkUid?:0L,
            UmAccountManager.getActivePersonUid(context), UMCalendarUtil.getDateInMilliPlusDays(0),
        comment, commentPublic)
        GlobalScope.launch {
            db.commentsDao.insertAsync(comment)
        }
    }



}