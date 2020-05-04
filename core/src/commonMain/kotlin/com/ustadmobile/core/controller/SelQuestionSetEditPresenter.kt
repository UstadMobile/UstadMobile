package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.SelQuestionSetEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
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


class SelQuestionSetEditPresenter(context: Any,
                          arguments: Map<String, String>, view: SelQuestionSetEditView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> =
                                          UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<SelQuestionSetEditView, SelQuestionSet>(context, arguments, view,
        lifecycleOwner, systemImpl, db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val selQuestionOneToManyJoinEditHelper =
            DefaultOneToManyJoinEditHelper<SelQuestionAndOptions>(
                    { it.selQuestion.selQuestionUid},
                    "state_SelQuestion_list", SelQuestionAndOptions.serializer().list,
                    SelQuestionAndOptions.serializer().list, this)
            { selQuestion.selQuestionUid = it }

    fun handleAddOrEditSelQuestion(selQuestion: SelQuestionAndOptions) {
        selQuestionOneToManyJoinEditHelper.onEditResult(selQuestion)
    }

    fun handleRemoveSelQuestion(selQuestion: SelQuestionAndOptions) {
        selQuestionOneToManyJoinEditHelper.onDeactivateEntity(selQuestion)
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        // code to onCreate to set the liveList on the view, e.g:
        view.selQuestionList = selQuestionOneToManyJoinEditHelper.liveList

    }


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SelQuestionSet? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        //this query uses a left join and will therefor have multiple rows for each question -
        // option combination (where the question is duplicated). The group by and map will
        // eliminate the duplicates so that we have a list of question objects, each with it's own
        // list of options.
        val questionAndOptions: List<SelQuestionAndOptionRow> = withTimeoutOrNull(2000) {
            db.selQuestionDao.findAllActiveQuestionsWithOpensInSetAsListAsc(entityUid)
        }?: listOf()
        val questionsWithOptionsList: List<SelQuestionAndOptions> =
                questionAndOptions.groupBy { it.selQuestion }.entries
                    .map { SelQuestionAndOptions(
                            it.key?:SelQuestion(),
                            it.value.map { it.selQuestionOption?:SelQuestionOption() }, listOf()) }

        // Load the list for any one to many join helper here
         val selQuestionSet = withTimeoutOrNull(2000) {
             db.selQuestionSetDao.findByUid(entityUid)
         } ?: SelQuestionSet()

        selQuestionOneToManyJoinEditHelper.liveList.sendValue(questionsWithOptionsList)
         return selQuestionSet
    }

    override fun onLoadFromJson(bundle: Map<String, String>): SelQuestionSet? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SelQuestionSet? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(SelQuestionSet.serializer(), entityJsonStr)
        }else {
            editEntity = SelQuestionSet()
        }

        selQuestionOneToManyJoinEditHelper.onLoadFromJsonSavedState(bundle)

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: SelQuestionSet) {

        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.selQuestionSetUid == 0L) {
                entity.selQuestionSetUid = repo.selQuestionSetDao.insertAsync(entity)
            }else {
                repo.selQuestionSetDao.updateAsync(entity)
            }

            val eti : List<SelQuestionAndOptions> =
                    selQuestionOneToManyJoinEditHelper.entitiesToInsert
            val etu : List<SelQuestionAndOptions> =
                    selQuestionOneToManyJoinEditHelper.entitiesToUpdate
            etu.forEach {
                val questionUid = it.selQuestion.selQuestionUid
                it.options.forEach {
                    it.selQuestionOptionQuestionUid = questionUid
                }
            }
            eti.forEach {
                it.selQuestion.selQuestionSelQuestionSetUid = entity.selQuestionSetUid
                it.selQuestion.selQuestionUid = 0L
                val questionUid = repo.selQuestionDao.insertAsync(it.selQuestion)
                it.selQuestion.selQuestionUid = questionUid
                it.options.forEach {
                    it.selQuestionOptionQuestionUid = questionUid
                }
            }

            repo.selQuestionDao.updateListAsync(etu.map { it.selQuestion })

            val allQuestions: List<SelQuestionAndOptions> = (eti + etu)
            val allOptions = allQuestions.flatMap { it.options }
            val splitList = allOptions.partition { it.selQuestionOptionUid == 0L }
            repo.selQuestionOptionDao.insertList(splitList.first)
            repo.selQuestionOptionDao.updateList(splitList.second)

            val deactivateOptions = allQuestions.flatMap { it.optionsToDeactivate }
            db.selQuestionOptionDao.deactivateByUids(deactivateOptions)

            val etd : List<SelQuestionAndOptions> =
                    selQuestionOneToManyJoinEditHelper.entitiesToDeactivate
            repo.selQuestionDao.deactivateByUids(etd.map {it.selQuestion.selQuestionUid })

            view.finishWithResult(listOf(entity))
        }
    }

    companion object {
    }

}