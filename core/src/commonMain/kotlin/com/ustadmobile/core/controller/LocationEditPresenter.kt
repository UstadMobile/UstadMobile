package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.LocationEditView
import com.ustadmobile.core.view.ProductEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.lib.db.entities.Location


class LocationEditPresenter(context: Any,
                            arguments: Map<String, String>, view: LocationEditView, di: DI,
                            lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<LocationEditView, Location>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Location? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
         val location = withTimeoutOrNull(2000) {
             db.locationDao.findByUidAsync(entityUid)
         } ?: Location()

         return location
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Location? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: Location? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(Location.serializer(), entityJsonStr)
        }else {
            editEntity = Location()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: Location) {

        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.locationUid == 0L) {
                entity.locationUid = repo.locationDao.insertAsync(entity)
            }else {
                repo.locationDao.updateAsync(entity)
            }

            view.finishWithResult(listOf(entity))
        }
    }

}