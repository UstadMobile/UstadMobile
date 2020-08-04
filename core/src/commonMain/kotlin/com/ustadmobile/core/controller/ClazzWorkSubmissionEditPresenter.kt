package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzWorkSubmissionEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzWorkSubmission
import com.ustadmobile.lib.db.entities.ClazzWorkSubmissionWithClazzWork
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kodein.di.DI


class ClazzWorkSubmissionEditPresenter(context: Any,
                          arguments: Map<String, String>, view: ClazzWorkSubmissionEditView,
                           di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ClazzWorkSubmissionEditView, ClazzWorkSubmissionWithClazzWork>(
        context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = TODO("PERSISTENCE_MODE.DB OR PERSISTENCE_MODE.JSON")

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWorkSubmissionWithClazzWork? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        //TODO: Load the list for any one to many join helper here
        /* e.g.
         val clazzWorkSubmission = withTimeoutOrNull {
             db.clazzWorkSubmission.findByUid(entityUid)
         } ?: ClazzWorkSubmission()
         return clazzWorkSubmission
         */
        return TODO("Implement load from Database or return null if using PERSISTENCE_MODE.JSON")
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzWorkSubmissionWithClazzWork? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ClazzWorkSubmission? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(ClazzWorkSubmissionWithClazzWork.serializer(), entityJsonStr)
        }else {
            editEntity = ClazzWorkSubmissionWithClazzWork()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: ClazzWorkSubmissionWithClazzWork) {
        //TODO: Any validation that is needed before accepting / saving this entity
        //TODO: Only save to the database when the persistence mode is PERSISTENCE_MODE.DB
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.clazzWorkSubmissionUid == 0L) {
                entity.clazzWorkSubmissionUid = repo.clazzWorkSubmissionDao.insertAsync(entity)
            }else {
                repo.clazzWorkSubmissionDao.updateAsync(entity)
            }

            //TODO: Call commitToDatabase on any onetomany join helpers
            //view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}