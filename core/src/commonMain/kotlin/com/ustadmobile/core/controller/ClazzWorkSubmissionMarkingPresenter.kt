package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZMEMBER_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZWORK_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
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

    private var filterByClazzWorkUid: Long = -1
    private var filterByClazzMemberUid: Long = -1
    private var unmarkedMembers: List<ClazzWorkSubmission> = listOf()

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzMemberAndClazzWorkWithSubmission? {

        filterByClazzWorkUid = arguments[ARG_CLAZZWORK_UID]?.toLong()?:0L
        filterByClazzMemberUid = arguments[ARG_CLAZZMEMBER_UID]?.toLong() ?: 0L

        val loggedInPersonUid = accountManager.activeAccount.personUid

        val clazzMemberAndClazzWorkWithSubmission = withTimeoutOrNull(2000){
            db.clazzWorkDao.findClazzMemberWithAndSubmissionWithPerson(filterByClazzWorkUid,
                    filterByClazzMemberUid)
        }

        val clazzWorkWithSubmission = withTimeoutOrNull(2000){
            db.clazzWorkDao.findWithSubmissionByUidAndPerson(filterByClazzWorkUid,
                    clazzMemberAndClazzWorkWithSubmission?.clazzMemberPersonUid?:0L)
        }?: ClazzWorkWithSubmission()

        //Build the quiz questions
        if(clazzMemberAndClazzWorkWithSubmission?.clazzWork?.clazzWorkSubmissionType
                == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ) {

            val questionAndOptions: List<ClazzWorkQuestionAndOptionRow> =
                    withTimeoutOrNull(2000) {
                        db.clazzWorkQuestionDao.findAllActiveQuestionsWithOptionsInClazzWorkAsList(
                                clazzMemberAndClazzWorkWithSubmission.clazzWork?.clazzWorkUid?:0L)
                    } ?: listOf()

            val questionsAndOptionsWithResponseList: List<ClazzWorkQuestionAndOptionWithResponse> =
                questionAndOptions.groupBy { it.clazzWorkQuestion }.entries.map {

                    val questionUid = it.key?.clazzWorkQuestionUid ?: 0L
                    val qResponse = db.clazzWorkQuestionResponseDao.findByQuestionUidAndClazzMemberUidAsync(
                            questionUid, clazzMemberAndClazzWorkWithSubmission.clazzMemberUid?:0L).toMutableList()
                    if (qResponse.isEmpty()) {
                        qResponse.add(ClazzWorkQuestionResponse().apply {
                            clazzWorkQuestionResponseQuestionUid = questionUid
                            clazzWorkQuestionResponsePersonUid = clazzMemberAndClazzWorkWithSubmission.clazzMemberPersonUid?:0L
                            clazzWorkQuestionResponseClazzMemberUid =
                                    clazzMemberAndClazzWorkWithSubmission.clazzMemberUid?: 0L
                            clazzWorkQuestionResponseClazzWorkUid =
                                    clazzMemberAndClazzWorkWithSubmission.clazzWork?.clazzWorkUid?: 0L
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


            //If submitted - show view data
            if(clazzMemberAndClazzWorkWithSubmission.submission != null
                    && clazzMemberAndClazzWorkWithSubmission.submission?.clazzWorkSubmissionUid != 0L){
                view.takeIf { it.quizSubmissionViewData == null
                }?.quizSubmissionViewData = DoorMutableLiveData(
                        questionsAndOptionsWithResponseList)
            }else{
                //No submission
                view.takeIf { it.quizSubmissionViewData == null
                }?.quizSubmissionViewData = DoorMutableLiveData(
                        listOf())
                view.takeIf { it.quizSubmissionEditData == null
                }?.quizSubmissionEditData = DoorMutableLiveData(
                        questionsAndOptionsWithResponseList)
            }
        }

        //Add a blank submission for marking to be saved to.
        if(clazzMemberAndClazzWorkWithSubmission?.clazzWork?.clazzWorkSubmissionType
                == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE) {
            //Add submission if it does not exist for none type:
            val submission = clazzMemberAndClazzWorkWithSubmission.submission ?:
            ClazzWorkSubmission().apply {
                clazzWorkSubmissionClazzWorkUid = clazzMemberAndClazzWorkWithSubmission.clazzWork?.clazzWorkUid?: 0L
                clazzWorkSubmissionClazzMemberUid = clazzMemberAndClazzWorkWithSubmission.clazzMemberUid ?: 0L
                clazzWorkSubmissionDateTimeFinished = getSystemTimeInMillis()
                clazzWorkSubmissionInactive = false
                clazzWorkSubmissionPersonUid = clazzMemberAndClazzWorkWithSubmission.clazzMemberPersonUid?:0L
            }

            if (submission.clazzWorkSubmissionUid == 0L) {
                submission.clazzWorkSubmissionUid = repo.clazzWorkSubmissionDao.insertAsync(submission)
            }
        }else{
            //Add un persisted submission if it does not exist for others type:
            val submission = clazzMemberAndClazzWorkWithSubmission?.submission ?:
            ClazzWorkSubmission().apply {
                clazzWorkSubmissionClazzWorkUid = clazzMemberAndClazzWorkWithSubmission?.clazzWork?.clazzWorkUid?: 0L
                clazzWorkSubmissionClazzMemberUid = clazzMemberAndClazzWorkWithSubmission?.clazzMemberUid ?: 0L
                clazzWorkSubmissionDateTimeFinished = getSystemTimeInMillis()
                clazzWorkSubmissionInactive = false
                clazzWorkSubmissionPersonUid = clazzMemberAndClazzWorkWithSubmission?.clazzMemberPersonUid?:0L
            }

            clazzMemberAndClazzWorkWithSubmission?.submission = submission
        }

        val privateComments = withTimeoutOrNull(2000) {
            repo.commentsDao.findPrivateCommentsByEntityTypeAndUidAndPersonAndPersonToLive(
                    ClazzWork.CLAZZ_WORK_TABLE_ID,
                    clazzMemberAndClazzWorkWithSubmission?.clazzWork?.clazzWorkUid?:0L,
                    clazzMemberAndClazzWorkWithSubmission?.clazzMemberPersonUid?:0L)
        }
        view.takeIf { it.privateComments == null}?.privateComments = privateComments

        newCommentItemListener.fromPerson = loggedInPersonUid
        newCommentItemListener.toPerson = clazzMemberAndClazzWorkWithSubmission?.clazzMemberPersonUid?:0L
        newCommentItemListener.entityId = clazzMemberAndClazzWorkWithSubmission?.clazzWork?.clazzWorkUid?:0L


        val clazzWorkWithMetrics =
                repo.clazzWorkDao.findClazzWorkWithMetricsByClazzWorkUidAsync(
                        filterByClazzWorkUid)

        view.runOnUiThread(Runnable {
            view.takeIf { it.clazzWorkMetrics == null}?.clazzWorkMetrics = clazzWorkWithMetrics
        })

        unmarkedMembers = withTimeoutOrNull(2000){
            db.clazzWorkSubmissionDao.findCompletedUnMarkedSubmissionsByClazzWorkUid(filterByClazzWorkUid)
        }?: listOf()

        //TODO: this first if clause doesn't do anything.
        if(unmarkedMembers.size == 1 && unmarkedMembers[0].clazzWorkSubmissionUid ==
                clazzMemberAndClazzWorkWithSubmission?.submission?.clazzWorkSubmissionUid ) !view.isMarkingFinished
        else if(unmarkedMembers.size == 1 && unmarkedMembers[0].clazzWorkSubmissionUid !=
                clazzMemberAndClazzWorkWithSubmission?.submission?.clazzWorkSubmissionUid){
            view.isMarkingFinished = true
        }else {
            view.isMarkingFinished = unmarkedMembers.size > 1
        }


        return clazzMemberAndClazzWorkWithSubmission
    }

    fun createSubmissionIfDoesNotExist(){
        GlobalScope.launch {
            val clazzMemberWithSubmission = withTimeoutOrNull(2000) {
                db.clazzWorkDao.findClazzMemberWithAndSubmissionWithPerson(filterByClazzWorkUid,
                        filterByClazzMemberUid)
            }

            val clazzWorkWithSubmission = withTimeoutOrNull(2000){
                db.clazzWorkDao.findWithSubmissionByUidAndPerson(filterByClazzWorkUid,
                        clazzMemberWithSubmission?.clazzMemberPersonUid?:0L)
            }?: ClazzWorkWithSubmission()

            if (clazzMemberWithSubmission?.clazzWork?.clazzWorkSubmissionType
                    == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT) {
                //Add submission if it does not exist for none type:
                val submission = clazzMemberWithSubmission.submission
                        ?: ClazzWorkSubmission().apply {
                            clazzWorkSubmissionClazzWorkUid = clazzMemberWithSubmission.clazzWork?.clazzWorkUid
                                    ?: 0L
                            clazzWorkSubmissionClazzMemberUid = clazzMemberWithSubmission.clazzMemberUid ?: 0L
                            clazzWorkSubmissionDateTimeFinished = getSystemTimeInMillis()
                            clazzWorkSubmissionInactive = false
                            clazzWorkSubmissionPersonUid = clazzMemberWithSubmission.person?.personUid?:0L
                        }

                submission.clazzWorkSubmissionDateTimeFinished = getSystemTimeInMillis()
                clazzWorkWithSubmission.clazzWorkSubmission = submission

                view.runOnUiThread(Runnable {
                    view.updatedSubmission = clazzWorkWithSubmission
                })

            }
        }
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

    //TODO: This is largely the same as what we have in ClazzWorkDetailOverview - what is duplicated
    // could be moved to an extension function on the database e.g. suspend fun UmAppDatabase.submitClazzWorkResponse(...)
    fun handleClickSubmitOnBehalf(){
        val questionsWithOptionsAndResponse =
                view.quizSubmissionEditData?.getValue()?: listOf()
        val newOptionsAndResponse = mutableListOf<ClazzWorkQuestionAndOptionWithResponse>()

        val updatedShortTextSubmission = view.updatedSubmission?.clazzWorkSubmission

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

            val studentClazzMember: ClazzMember? = withTimeoutOrNull(2000){
                db.clazzMemberDao.findByPersonUidAndClazzUidAsync(
                        entity?.person?.personUid?:0L,
                        entity?.clazzWork?.clazzWorkClazzUid?:0L)
            }
            var submission: ClazzWorkSubmission? = null

            if(entity?.clazzWork?.clazzWorkSubmissionType ==
                    ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT &&
                    updatedShortTextSubmission != null){
                submission = updatedShortTextSubmission

            }else if(entity?.clazzWork?.clazzWorkSubmissionType ==
                    ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ) {


                submission = entity?.submission ?: ClazzWorkSubmission().apply {
                    clazzWorkSubmissionClazzWorkUid = clazzWorkWithSubmission?.clazzWork?.clazzWorkUid
                            ?: 0L
                    clazzWorkSubmissionClazzMemberUid = studentClazzMember?.clazzMemberUid ?: 0L
                    clazzWorkSubmissionDateTimeFinished = getSystemTimeInMillis()
                    clazzWorkSubmissionInactive = false
                    clazzWorkSubmissionPersonUid = studentClazzMember?.clazzMemberPersonUid ?: 0L
                }
            }

            submission?.clazzWorkSubmissionDateTimeFinished = getSystemTimeInMillis()

            if(submission?.clazzWorkSubmissionUid == 0L ) {
                submission?.clazzWorkSubmissionUid = repo.clazzWorkSubmissionDao.insertAsync(submission)
            }else if (submission != null ){
                repo.clazzWorkSubmissionDao.updateAsync(submission)
            }

            clazzWorkWithSubmission?.submission = submission
            view.runOnUiThread(Runnable {
                view.entity = clazzWorkWithSubmission
                view.quizSubmissionEditData = DoorMutableLiveData(newOptionsAndResponse)
            })

        }
    }

    fun handleClickSaveAndMarkNext(showNext: Boolean?){

        val entityFromView = view.entity
        val next = showNext ?: true

        if (entityFromView != null) {
            handleClickSaveWithMovement(entityFromView, !next)
        }

        //TODO: Let's not use SystemImpl.go here.
        if(next) {
            val nextClazzMemberUid = unmarkedMembers[0].clazzWorkSubmissionClazzMemberUid

            systemImpl.go(ClazzWorkSubmissionMarkingView.VIEW_NAME,
                    mapOf(ARG_CLAZZWORK_UID to filterByClazzWorkUid.toString(),
                            ARG_CLAZZMEMBER_UID to nextClazzMemberUid.toString()),
                    context)
        }
    }

    private fun handleClickSaveWithMovement(entity: ClazzMemberAndClazzWorkWithSubmission,
                                            leave: Boolean){
        GlobalScope.launch(doorMainDispatcher()) {
            val submission = entity.submission
            //If submission exists
            if(submission  != null) {
                //TODO: why not just use systemTimeInMillis()?
                submission.clazzWorkSubmissionDateTimeMarked =
                        UMCalendarUtil.getDateInMilliPlusDays(0)
                submission.clazzWorkSubmissionMarkerPersonUid =
                        accountManager.activeAccount.personUid
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