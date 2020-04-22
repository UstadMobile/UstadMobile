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
import kotlinx.serialization.json.Json
import kotlinx.serialization.list


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

    /*
     * TODOne: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
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

        // code to the onLoadEntityFromDb to set the list if loading from the database, e.g.

        val questionAndOptions: List<SelQuestionAndOptionRow> = withTimeoutOrNull(2000) {
            db.selQuestionDao.findAllActiveQuestionsWithOpensInSetAsListAsc(entityUid)
        }?: listOf()
        val questionsWithOptionsList: List<SelQuestionAndOptions> =
                questionAndOptions.groupBy { it.selQuestion }.entries
                    .map { SelQuestionAndOptions(
                            it.key?:SelQuestion(),
                            it.value.map { it.selQuestionOption?:SelQuestionOption() }) }

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
            eti.iterator().forEach {
                val options = it.options
                val question = it.selQuestion
                question.selQuestionSelQuestionSetUid = entity.selQuestionSetUid
                question.selQuestionUid = 0L
                //Since all are for insert only
                question.selQuestionUid = repo.selQuestionDao.insertAsync(question)
                options?.forEach {
                    //TODO: Check if insert or update
                    it.selQuestionOptionQuestionUid = question.selQuestionUid
                    if(it.selQuestionOptionUid < 1) {
                        it.selQuestionOptionUid = 0L
                        repo.selQuestionOptionDao.insertAsync(it)
                    }else{
                        repo.selQuestionOptionDao.updateAsync(it)
                    }
                }
            }

            val etu : List<SelQuestionAndOptions> =
                    selQuestionOneToManyJoinEditHelper.entitiesToUpdate
            etu.iterator().forEach {
                val options = it.options
                val question = it.selQuestion
                question.selQuestionSelQuestionSetUid = entity.selQuestionSetUid
                //Since all are for update only
                repo.selQuestionDao.updateAsync(question)
                options?.forEach {
                    //TODO: Check if insert or update
                    it.selQuestionOptionQuestionUid = question.selQuestionUid
                    if(it.selQuestionOptionUid < 1) {
                        it.selQuestionOptionUid = 0L
                        repo.selQuestionOptionDao.insertAsync(it)
                    }else{
                        repo.selQuestionOptionDao.updateAsync(it)
                    }
                }
            }

            //TODO: Check why it is not working
            val etd : List<SelQuestionAndOptions> =
                    selQuestionOneToManyJoinEditHelper.entitiesToDeactivate
            etd.iterator().forEach {
                val question  = it.selQuestion
                question.questionActive = false
                repo.selQuestionDao.updateAsync(question)
            }

            view.finishWithResult(listOf(entity))
        }
    }

    companion object {
    }

}