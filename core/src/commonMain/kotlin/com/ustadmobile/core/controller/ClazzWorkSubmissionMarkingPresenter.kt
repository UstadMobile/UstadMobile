package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZMEMBER_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZWORK_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import org.kodein.di.DI


class ClazzWorkSubmissionMarkingPresenter(context: Any,
              arguments: Map<String, String>, view: ClazzWorkSubmissionMarkingView, di: DI,
              lifecycleOwner: DoorLifecycleOwner,
              private val newCommentItemListener: DefaultNewCommentItemListener =
                      DefaultNewCommentItemListener(di, context))
    : UstadEditPresenter<ClazzWorkSubmissionMarkingView,
        ClazzMemberAndClazzWorkWithSubmission>(context, arguments, view, di, lifecycleOwner),
        NewCommentItemListener by newCommentItemListener  {

    var filterByClazzWorkUid: Long = -1
    var filterByClazzMemberUid: Long = -1
    var unmarkedMembers: List<ClazzWorkSubmission> = listOf()

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        filterByClazzWorkUid = arguments[ARG_CLAZZWORK_UID]?.toLong()?:0L
        filterByClazzMemberUid = arguments[ARG_CLAZZMEMBER_UID]?.toLong() ?: 0L
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzMemberAndClazzWorkWithSubmission? {
        val loggedInPersonUid = accountManager.activeAccount.personUid

        val clazzMemberWithSubmission = withTimeoutOrNull(2000){
            db.clazzWorkDao.findClazzMemberWithAndSubmissionWithPerson(filterByClazzWorkUid,
                    filterByClazzMemberUid)
        }


        unmarkedMembers = withTimeoutOrNull(2000){
            db.clazzWorkSubmissionDao.findCompletedUnMarkedSubmissionsByClazzWorkUid(filterByClazzWorkUid)
        }?: listOf()

        if(unmarkedMembers.size == 1 && unmarkedMembers[0].clazzWorkSubmissionUid ==
                clazzMemberWithSubmission?.submission?.clazzWorkSubmissionUid ){
            view.isMarkingFinished == false
        }else if(unmarkedMembers.size == 1 && unmarkedMembers[0].clazzWorkSubmissionUid !=
                clazzMemberWithSubmission?.submission?.clazzWorkSubmissionUid){
            view.isMarkingFinished = true
        }else {
            view.isMarkingFinished = unmarkedMembers.size > 1
        }

        val clazzMember: ClazzMember? = withTimeoutOrNull(2000){
            db.clazzMemberDao.findByUid(filterByClazzMemberUid)
        }
        val clazzWorkWithSubmission = withTimeoutOrNull(2000){
            db.clazzWorkDao.findWithSubmissionByUidAndPerson(filterByClazzWorkUid,
                    clazzMemberWithSubmission?.clazzMemberPersonUid?:0L)
        }?: ClazzWorkWithSubmission()

        val submission = clazzMemberWithSubmission?.submission
        if(submission != null && submission.clazzWorkSubmissionUid != 0L &&
                clazzMemberWithSubmission.clazzWork?.clazzWorkSubmissionType
                == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ) {
            val questionAndOptions: List<ClazzWorkQuestionAndOptionRow> =
                    withTimeoutOrNull(2000) {
                        db.clazzWorkQuestionDao.findAllActiveQuestionsWithOptionsInClazzWorkAsList(
                                clazzMemberWithSubmission.clazzWork?.clazzWorkUid?:0L)
                    } ?: listOf()

            val questionsAndOptionsWithResponseList: List<ClazzWorkQuestionAndOptionWithResponse> =
                questionAndOptions.groupBy { it.clazzWorkQuestion }.entries.map {

                    val questionUid = it.key?.clazzWorkQuestionUid ?: 0L
                    val qResponse = db.clazzWorkQuestionResponseDao.findByQuestionUidAndClazzMemberUidAsync(
                            questionUid, clazzMember?.clazzMemberUid?:0L).toMutableList()
                    if (qResponse.isEmpty()) {
                        qResponse.add(ClazzWorkQuestionResponse().apply {
                            clazzWorkQuestionResponseQuestionUid = questionUid
                            clazzWorkQuestionResponsePersonUid = clazzMember?.clazzMemberPersonUid?:0L
                            clazzWorkQuestionResponseClazzMemberUid =
                                    clazzMember?.clazzMemberUid?: 0L
                            clazzWorkQuestionResponseClazzWorkUid =
                                    clazzMemberWithSubmission.clazzWork?.clazzWorkUid?: 0L
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

            view.submissionQuestionAndOptionsWithResponse =
                    DoorMutableLiveData(questionsAndOptionsWithResponseList)
        }

        val privateComments = withTimeoutOrNull(2000) {
            db.commentsDao.findPrivateCommentsByEntityTypeAndUidAndPersonAndPersonToLive(
                    ClazzWork.CLAZZ_WORK_TABLE_ID, clazzWorkWithSubmission.clazzWorkUid,
                    clazzMember?.clazzMemberPersonUid?:0L, loggedInPersonUid)
        }
        view.privateComments = privateComments

        val test = withTimeoutOrNull(2000){
            db.commentsDao.findPrivateCommentsByEntityTypeAndUidAndPersonAndPersonToTest(
                    ClazzWork.CLAZZ_WORK_TABLE_ID, clazzWorkWithSubmission.clazzWorkUid,
                    clazzMember?.clazzMemberPersonUid?:0L, loggedInPersonUid)

        }

        newCommentItemListener.fromPerson = loggedInPersonUid
        newCommentItemListener.toPerson = clazzMember?.clazzMemberPersonUid?:0L
        newCommentItemListener.entityId = clazzWorkWithSubmission.clazzWorkUid


        val clazzWorkWithMetrics =
                repo.clazzWorkDao.findClazzWorkWithMetricsByClazzWorkUidAsync(
                        filterByClazzWorkUid)

        view.runOnUiThread(Runnable {
            view.clazzWorkMetrics = clazzWorkWithMetrics
        })

        return clazzMemberWithSubmission
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

    fun handleClickSaveAndMarkNext(entity: ClazzMemberAndClazzWorkWithSubmission?, showNext: Boolean?){

        val entityFromView = view.entity
        val next = showNext ?: true

        if (entityFromView != null) {
            handleClickSaveWithMovement(entityFromView, !next)
        }

        //unmarkedMembers
        if(next) {
            val nextClazzMemberUid = unmarkedMembers.get(0).clazzWorkSubmissionClazzMemberUid



            systemImpl.go(ClazzWorkSubmissionMarkingView.VIEW_NAME,
                    mapOf(ARG_CLAZZWORK_UID to filterByClazzWorkUid.toString(),
                            ARG_CLAZZMEMBER_UID to nextClazzMemberUid.toString()),
                    context)

        }
    }

    private fun handleClickSaveWithMovement(entity: ClazzMemberAndClazzWorkWithSubmission, leave: Boolean){
        GlobalScope.launch(doorMainDispatcher()) {
            val submission = entity.submission
            //If submission exists
            if(submission  != null) {
                submission.clazzWorkSubmissionDateTimeMarked =
                        UMCalendarUtil.getDateInMilliPlusDays(0)
                if(submission.clazzWorkSubmissionUid != 0L) {
                    repo.clazzWorkSubmissionDao.updateAsync(submission)
                }else{
                    repo.clazzWorkSubmissionDao.insertAsync(submission)
                }
            }

            if(leave) {
                view.finishWithResult(listOf(entity))
            }
        }
    }

    override fun handleClickSave(entity: ClazzMemberAndClazzWorkWithSubmission) {
        handleClickSaveWithMovement(entity, true)
    }
}