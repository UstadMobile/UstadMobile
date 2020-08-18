package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.EntityRoleEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import org.kodein.di.DI


class EntityRoleEditPresenter(context: Any,
                              arguments: Map<String, String>, view: EntityRoleEditView, di : DI,
                              lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<EntityRoleEditView, EntityRoleWithNameAndRole>(context, arguments, view,
         di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): EntityRoleWithNameAndRole? {
        val entityRoleUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L

        val entityRole = withTimeoutOrNull(2000) {
            db.entityRoleDao.takeIf {entityRoleUid != 0L }?.findWithNameAndRoleByUidAsync(entityRoleUid)
        }  ?: EntityRoleWithNameAndRole().also { entityRole ->
            entityRole.erActive = true
        }

        return entityRole
    }

    override fun onLoadFromJson(bundle: Map<String, String>): EntityRoleWithNameAndRole? {
        super.onLoadFromJson(bundle)
        val entityRoleJsonStr = bundle[ARG_ENTITY_JSON]
        var entityRole: EntityRoleWithNameAndRole? = null
        if(entityRoleJsonStr != null) {
            entityRole = Json.parse(EntityRoleWithNameAndRole.serializer(), entityRoleJsonStr)
        }else {
            entityRole = EntityRoleWithNameAndRole()
        }

        return entityRole
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity ?: return
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                    entityVal)
    }

    override fun handleClickSave(entity: EntityRoleWithNameAndRole) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.erRoleUid == 0L) {
                repo.entityRoleDao.insertAsync(entity)
            }else {
                repo.entityRoleDao.updateAsync(entity)
            }

            onFinish("", entity.erUid, entity)
        }
    }


}