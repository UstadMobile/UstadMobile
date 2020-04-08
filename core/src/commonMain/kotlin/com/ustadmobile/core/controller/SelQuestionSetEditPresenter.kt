package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.SelQuestionSetEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.SelQuestionSet

import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.lib.db.entities.SelQuestion


class SelQuestionSetEditPresenter(context: Any,
                          arguments: Map<String, String>, view: SelQuestionSetEditView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<SelQuestionSetEditView, SelQuestionSet>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    val selQuestionOneToManyJoinEditHelper = DefaultOneToManyJoinEditHelper<SelQuestion>(SelQuestion::selQuestionUid,
            "state_SelQuestion_list", SelQuestion.serializer().list,
            SelQuestion.serializer().list, this) { selQuestionUid = it }

    fun handleAddOrEditSelQuestion(selQuestion: SelQuestion) {
        selQuestionOneToManyJoinEditHelper.onEditResult(selQuestion)
    }

    fun handleRemoveSelQuestion(selQuestion: SelQuestion) {
        selQuestionOneToManyJoinEditHelper.onDeactivateEntity(selQuestion)
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        // code to onCreate to set the liveList on the view, e.g:
        view.selQuestionList = selQuestionOneToManyJoinEditHelper.liveList

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SelQuestionSet? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        // code to the onLoadEntityFromDb to set the list if loading from the database, e.g.
         val selQuestionList = withTimeoutOrNull(2000) {
            db.selQuestionDao.findAllActiveQuestionsInSetAsList(entityUid)
         }?: listOf()

         selQuestionOneToManyJoinEditHelper.liveList.sendValue(selQuestionList)

        // Load the list for any one to many join helper here
         val selQuestionSet = withTimeoutOrNull(2000) {
             db.selQuestionSetDao.findByUid(entityUid)
         } ?: SelQuestionSet()
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

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: SelQuestionSet) {
        //TODO: Any validation that is needed before accepting / saving this entity
        //TODO: Only save to the database when the persistence mode is PERSISTENCE_MODE.DB
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.selQuestionSetUid == 0L) {
                entity.selQuestionSetUid = repo.selQuestionSetDao.insertAsync(entity)
            }else {
                repo.selQuestionSetDao.updateAsync(entity)
            }

            //TODO: Call commitToDatabase on any onetomany join helpers
            // code to handleClickSave to save the result to the database
            selQuestionOneToManyJoinEditHelper.commitToDatabase(repo.selQuestionDao) {
               it.selQuestionSelQuestionSetUid = entity.selQuestionSetUid
             }


            view.finishWithResult(entity)
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}