package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzWorkEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json


class ClazzWorkEditPresenter(context: Any,
                          arguments: Map<String, String>, view: ClazzWorkEditView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<ClazzWorkEditView, ClazzWork>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    enum class SubmissionOptions(val optionVal: Int, val messageId: Int){
        NO_SUBMISSION_REQUIRED(ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                MessageID.no_submission_required),
        SHORT_TEXT(ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT,
                MessageID.short_text),
        ATTACHMENT(ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_ATTACHMENT,
                MessageID.attachment),
        QUIZ(ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ,
                MessageID.quiz),
    }

    class SubmissionOptionsMessageIdOption(day: SubmissionOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    val contentJoinEditHelper = DefaultOneToManyJoinEditHelper<ContentEntryWithMetrics>(
            ContentEntryWithMetrics::contentEntryUid,
            "state_ContentEntryWithMetrics_list",
            ContentEntryWithMetrics.serializer().list,
            ContentEntryWithMetrics.serializer().list, this) { contentEntryUid = it }

    fun handleAddOrEditContent(entityClass: ContentEntryWithMetrics) {
        contentJoinEditHelper.onEditResult(entityClass)
    }

    fun handleRemoveContent(entityClass: ContentEntryWithMetrics) {
        contentJoinEditHelper.onDeactivateEntity(entityClass)
    }

    val questionAndOptionsEditHelper =
            DefaultOneToManyJoinEditHelper<ClazzWorkQuestionAndOptions>(
            {it.clazzWorkQuestion.clazzWorkQuestionUid},
            "state_ClazzWorkQuestionAndOption_list",
                    ClazzWorkQuestionAndOptions.serializer().list,
            ClazzWorkQuestionAndOptions.serializer().list, this)
    { clazzWorkQuestion.clazzWorkQuestionUid = it }

    fun handleAddOrEditClazzQuestionAndOptions(entityClass: ClazzWorkQuestionAndOptions) {
        questionAndOptionsEditHelper.onEditResult(entityClass)
    }

    fun handleRemoveQuestionAndOptions(entityClass: ClazzWorkQuestionAndOptions) {
        questionAndOptionsEditHelper.onDeactivateEntity(entityClass)
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.clazzWorkQuizQuestionsAndOptions = questionAndOptionsEditHelper.liveList
        view.clazzWorkContent = contentJoinEditHelper.liveList
        view.submissionTypeOptions = SubmissionOptions.values().map{SubmissionOptionsMessageIdOption(it, context)}
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWork? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazzWork = withTimeoutOrNull(2000){
            db.clazzWorkDao.findByUidAsync(entityUid)
        }?:ClazzWork()

        val contentList = withTimeoutOrNull(2000) {
            db.clazzWorkContentJoinDao.findContentByClazzWorkUid(
                    clazzWork.clazzWorkUid,
                    clazzWork.clazzWorkStartDateTime,
                    clazzWork.clazzWorkDueDateTime)
         }?: listOf()

        contentJoinEditHelper.liveList.sendValue(contentList)

        val questionAndOptions: List<ClazzWorkQuestionAndOptionRow> =
                withTimeoutOrNull(2000) {
            db.clazzWorkQuestionDao.findAllActiveQuestionsWithOptionsInClazzWorkAsList(entityUid)
        }?: listOf()

        val questionsWithOptionsList: List<ClazzWorkQuestionAndOptions> =
                questionAndOptions.groupBy { it.clazzWorkQuestion }.entries
                        .map { ClazzWorkQuestionAndOptions(
                            it.key?: ClazzWorkQuestion(),
                            it.value.map { it.clazzWorkQuestionOption?: ClazzWorkQuestionOption() },
                                listOf()) }

        questionAndOptionsEditHelper.liveList.sendValue(questionsWithOptionsList)

        return clazzWork
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzWork? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ClazzWork? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(ClazzWork.serializer(), entityJsonStr)
        }else {
            editEntity = ClazzWork()
        }

        questionAndOptionsEditHelper.onLoadFromJsonSavedState(bundle)

        //TODO: the same for contentedithelper

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    var finishFragment : Boolean= true

    fun handleClickSaveOnly(entity: ClazzWork, finish: Boolean): Long{
        finishFragment = finish
        //handleClickSave(entity)


        val clazzUid = arguments[ARG_CLAZZ_UID]?.toLong() ?: 0L
        GlobalScope.launch(doorMainDispatcher()) {
            if (entity.clazzWorkUid == 0L) {
                entity.clazzWorkClazzUid = clazzUid
                entity.clazzWorkUid = repo.clazzWorkDao.insertAsync(entity)
            } else {
                repo.clazzWorkDao.updateAsync(entity)
            }

            //TODO: Replace with right thingi.
            // Not committing as this will change anyway
            //contentJoinEditHelper.commitToDatabase(repo.contentEntryDao)


            val eti: List<ClazzWorkQuestionAndOptions> =
                    questionAndOptionsEditHelper.entitiesToInsert
            val etu: List<ClazzWorkQuestionAndOptions> =
                    questionAndOptionsEditHelper.entitiesToUpdate
            etu.forEach {
                val questionUid = it.clazzWorkQuestion.clazzWorkQuestionUid
                it.options.forEach {
                    it.clazzWorkQuestionOptionUid = questionUid
                }
            }
            eti.forEach {
                it.clazzWorkQuestion.clazzWorkQuestionClazzWorkUid = entity.clazzWorkUid
                it.clazzWorkQuestion.clazzWorkQuestionUid = 0L
                val questionUid = repo.clazzWorkQuestionDao.insertAsync(it.clazzWorkQuestion)
                it.clazzWorkQuestion.clazzWorkQuestionUid = questionUid
                it.options.forEach {
                    it.clazzWorkQuestionOptionQuestionUid = questionUid
                }
            }

            repo.clazzWorkQuestionDao.updateListAsync(etu.map { it.clazzWorkQuestion })

            val allQuestions: List<ClazzWorkQuestionAndOptions> = (eti + etu)
            val allOptions = allQuestions.flatMap { it.options }
            val splitList = allOptions.partition { it.clazzWorkQuestionOptionUid == 0L }
            repo.clazzWorkQuestionOptionDao.insertList(splitList.first)
            repo.clazzWorkQuestionOptionDao.updateList(splitList.second)

            val deactivateOptions = allQuestions.flatMap { it.optionsToDeactivate }
            db.clazzWorkQuestionOptionDao.deactivateByUids(deactivateOptions)

            repo.clazzWorkQuestionDao.deactivateByUids(questionAndOptionsEditHelper.primaryKeysToDeactivate)

        }

        return entity.clazzWorkUid
    }

    override fun handleClickSave(entity: ClazzWork) {

        val clazzUid = arguments[ARG_CLAZZ_UID]?.toLong() ?: 0L
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.clazzWorkUid == 0L) {
                entity.clazzWorkClazzUid = clazzUid
                entity.clazzWorkUid = repo.clazzWorkDao.insertAsync(entity)
            }else {
                repo.clazzWorkDao.updateAsync(entity)
            }

            //TODO: Replace with right thingi.
            // Not committing as this will change anyway
            //contentJoinEditHelper.commitToDatabase(repo.contentEntryDao)


            val eti : List<ClazzWorkQuestionAndOptions> =
                    questionAndOptionsEditHelper.entitiesToInsert
            val etu : List<ClazzWorkQuestionAndOptions> =
                    questionAndOptionsEditHelper.entitiesToUpdate
            etu.forEach {
                val questionUid = it.clazzWorkQuestion.clazzWorkQuestionUid
                it.options.forEach {
                    it.clazzWorkQuestionOptionUid = questionUid
                }
            }
            eti.forEach {
                it.clazzWorkQuestion.clazzWorkQuestionClazzWorkUid = entity.clazzWorkUid
                it.clazzWorkQuestion.clazzWorkQuestionUid = 0L
                val questionUid = repo.clazzWorkQuestionDao.insertAsync(it.clazzWorkQuestion)
                it.clazzWorkQuestion.clazzWorkQuestionUid = questionUid
                it.options.forEach {
                    it.clazzWorkQuestionOptionQuestionUid = questionUid
                }
            }

            repo.clazzWorkQuestionDao.updateListAsync(etu.map { it.clazzWorkQuestion })

            val allQuestions: List<ClazzWorkQuestionAndOptions> = (eti + etu)
            val allOptions = allQuestions.flatMap { it.options }
            val splitList = allOptions.partition { it.clazzWorkQuestionOptionUid == 0L }
            repo.clazzWorkQuestionOptionDao.insertList(splitList.first)
            repo.clazzWorkQuestionOptionDao.updateList(splitList.second)

            val deactivateOptions = allQuestions.flatMap { it.optionsToDeactivate }
            db.clazzWorkQuestionOptionDao.deactivateByUids(deactivateOptions)

            repo.clazzWorkQuestionDao.deactivateByUids(questionAndOptionsEditHelper.primaryKeysToDeactivate)


            if(finishFragment) {
                view.finishWithResult(listOf(entity))
            }
        }
    }

    companion object {
    }

}