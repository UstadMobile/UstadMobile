package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.HolidayEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Holiday
import kotlinx.serialization.json.Json
import org.kodein.di.DI


class HolidayEditPresenter(context: Any,
                          arguments: Map<String, String>, view: HolidayEditView,
                           di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<HolidayEditView, Holiday>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Holiday? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        throw IllegalStateException("Holiday loads only from JSON, not from database")
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Holiday? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: Holiday? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, Holiday.serializer(), entityJsonStr)
        }else {
            editEntity = Holiday()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: Holiday) {
        view.finishWithResult(listOf(entity))
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}