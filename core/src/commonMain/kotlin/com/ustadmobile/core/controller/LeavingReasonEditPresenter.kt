package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.LeavingReasonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.LeavingReason

import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse


class LeavingReasonEditPresenter(context: Any,
        arguments: Map<String, String>, view: LeavingReasonEditView,
        lifecycleOwner: DoorLifecycleOwner,
        di: DI)
    : UstadEditPresenter<LeavingReasonEditView, LeavingReason>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): LeavingReason? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        //TODO: Load the list for any one to many join helper here
        /* e.g.
         val leavingReason = withTimeoutOrNull {
             db.leavingReason.findByUid(entityUid)
         } ?: LeavingReason()
         return leavingReason
         */
        return TODO("Implement load from Database or return null if using PERSISTENCE_MODE.JSON")
    }

    override fun onLoadFromJson(bundle: Map<String, String>): LeavingReason? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: LeavingReason? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, LeavingReason.serializer(), entityJsonStr)
        }else {
            editEntity = LeavingReason()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: LeavingReason) {
        //TODO: Any validation that is needed before accepting / saving this entity
        //TODO: Only save to the database when the persistence mode is PERSISTENCE_MODE.DB
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.leavingReasonUid == 0L) {
                entity.leavingReasonUid = repo.leavingReasonDao.insertAsync(entity)
            }else {
                repo.leavingReasonDao.updateAsync(entity)
            }

            //TODO: Call commitToDatabase on any onetomany join helpers
            view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}