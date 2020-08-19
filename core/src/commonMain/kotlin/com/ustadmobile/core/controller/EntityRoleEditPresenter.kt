package com.ustadmobile.core.controller

import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.EntityRoleEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import kotlinx.serialization.json.Json
import org.kodein.di.DI


class EntityRoleEditPresenter(context: Any,
                              arguments: Map<String, String>, view: EntityRoleEditView, di : DI,
                              lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<EntityRoleEditView, EntityRoleWithNameAndRole>(context, arguments, view,
         di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    override fun onLoadFromJson(bundle: Map<String, String>): EntityRoleWithNameAndRole? {
        super.onLoadFromJson(bundle)
        val entityRoleJsonStr = bundle[ARG_ENTITY_JSON]
        val personGroupUid = bundle[UstadView.ARG_FILTER_BY_PERSONGROUPUID]?.toLong()?:0L
        var entityRole: EntityRoleWithNameAndRole? = null
        if(entityRoleJsonStr != null) {
            entityRole = Json.parse(EntityRoleWithNameAndRole.serializer(), entityRoleJsonStr)
        }else {
            entityRole = EntityRoleWithNameAndRole()
        }
        entityRole.erGroupUid = personGroupUid

        return entityRole
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity ?: return
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                    entityVal)
    }

    override fun handleClickSave(entity: EntityRoleWithNameAndRole) {
        view.finishWithResult(listOf(entity))
    }


}