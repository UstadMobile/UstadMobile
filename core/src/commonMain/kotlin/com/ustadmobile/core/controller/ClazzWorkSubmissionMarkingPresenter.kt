package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZMEMBER_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZWORK_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json


class ClazzWorkSubmissionMarkingPresenter(context: Any,
              arguments: Map<String, String>, view: ClazzWorkSubmissionMarkingView,
              lifecycleOwner: DoorLifecycleOwner,
              systemImpl: UstadMobileSystemImpl,
              db: UmAppDatabase, repo: UmAppDatabase,
              activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData,
              private val newCommentItemListener: DefaultNewCommentItemListener =
                      DefaultNewCommentItemListener(db, context))
    : UstadEditPresenter<ClazzWorkSubmissionMarkingView,
        ClazzMemberAndClazzWorkWithSubmission>(context, arguments, view, lifecycleOwner, systemImpl,
            db, repo, activeAccount), NewCommentItemListener by newCommentItemListener  {

    var filterByClazzWorkUid: Long = -1

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        filterByClazzWorkUid = arguments[ARG_CLAZZWORK_UID]?.toLong()?:0
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzMemberAndClazzWorkWithSubmission? {
        val clazzWorkUid = arguments[ARG_CLAZZWORK_UID]?.toLong() ?: 0L
        val clazzMemberUid = arguments[ARG_CLAZZMEMBER_UID]?.toLong() ?: 0L
        val loggedInPersonUid = UmAccountManager.getActivePersonUid(context)

        GlobalScope.launch {
            entity = repo.clazzWorkDao.findClazzMemberWithAndSubmissionWithPerson(clazzWorkUid,
                clazzMemberUid)
//            view.runOnUiThread(Runnable {
//                view.entity = entity
//            })
        }

        val clazzMember: ClazzMember? = withTimeoutOrNull(2000){
            db.clazzMemberDao.findByUid(clazzMemberUid)
        }
        val clazzWorkWithSubmission = withTimeoutOrNull(2000){
            db.clazzWorkDao.findWithSubmissionByUidAndPerson(clazzWorkUid, entity?.clazzMemberPersonUid?:0L)
        }?: ClazzWorkWithSubmission()

        if(entity?.clazzWork?.clazzWorkSubmissionType == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ) {
            val questionAndOptions: List<ClazzWorkQuestionAndOptionRow> =
                    withTimeoutOrNull(2000) {
                        db.clazzWorkQuestionDao.findAllActiveQuestionsWithOptionsInClazzWorkAsList(
                                entity?.clazzWork?.clazzWorkUid?:0L)
                    } ?: listOf()

            val questionsAndOptionsWithResponseList: List<ClazzWorkQuestionAndOptionWithResponse> =
                    questionAndOptions.groupBy { it.clazzWorkQuestion }.entries
                            .map {
                                val questionUid = it.key?.clazzWorkQuestionUid ?: 0L
                                val qResponse = db.clazzWorkQuestionResponseDao.findByQuestionUidAsync(
                                        questionUid).toMutableList()
                                if (qResponse.isEmpty()) {
                                    qResponse.add(ClazzWorkQuestionResponse().apply {
                                        clazzWorkQuestionResponseQuestionUid = questionUid
                                        clazzWorkQuestionResponsePersonUid = loggedInPersonUid
                                        clazzWorkQuestionResponseClazzMemberUid = clazzMember?.clazzMemberUid
                                                ?: 0L
                                        clazzWorkQuestionResponseClazzWorkUid = entity?.clazzWork?.clazzWorkUid
                                                ?: 0L

                                    })
                                }
                                ClazzWorkQuestionAndOptionWithResponse(
                                        clazzWorkWithSubmission,
                                        it.key ?: ClazzWorkQuestion(),
                                        it.value.map {
                                            it.clazzWorkQuestionOption ?: ClazzWorkQuestionOption()
                                        },
                                        qResponse.first())
                            }


            view.clazzWorkQuizQuestionsAndOptionsWithResponse =
                    DoorMutableLiveData(questionsAndOptionsWithResponseList)
        }

        //TOOD: Update
        val privateComments = withTimeoutOrNull(2000) {
            db.commentsDao.findPrivateByEntityTypeAndUidAndPersonLive(ClazzWork.CLAZZ_WORK_TABLE_ID,
                    clazzWorkWithSubmission.clazzWorkUid, loggedInPersonUid)
        }
        view.privateCommentsToPerson = privateComments


        return entity
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzMemberAndClazzWorkWithSubmission? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ClazzMemberAndClazzWorkWithSubmission? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(ClazzMemberAndClazzWorkWithSubmission.serializer(), entityJsonStr)
        }else {
            editEntity = ClazzMemberAndClazzWorkWithSubmission()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: ClazzMemberAndClazzWorkWithSubmission) {

        GlobalScope.launch(doorMainDispatcher()) {
            val submission = entity?.submission
            //If submission exists
            if(submission  != null) {
                if(submission.clazzWorkSubmissionUid != 0L) {
                    repo.clazzWorkSubmissionDao.updateAsync(submission)
                }else{
                    repo.clazzWorkSubmissionDao.insertAsync(submission)
                }
            }

            view.finishWithResult(listOf(entity))
        }
    }
}