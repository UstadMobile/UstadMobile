package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.@BaseFileName@View
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.@Entity@
@EditEntity_Import@
import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse


class @BaseFileName@Presenter(context: Any,
        arguments: Map<String, String>, view: @BaseFileName@View,
        lifecycleOwner: LifecycleOwner,
        di: DI)
    : UstadEditPresenter<@BaseFileName@View, @EditEntity@>(context, arguments, view, di, lifecycleOwner) {

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

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): @EditEntity@? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        //TODO: Load the list for any one to many join helper here
        /* e.g.
         val @Entity_VariableName@ = withTimeoutOrNull {
             db.@Entity_VariableName@.findByUid(entityUid)
         } ?: @Entity@()
         return @Entity_VariableName@
         */
        return TODO("Implement load from Database or return null if using PERSISTENCE_MODE.JSON")
    }

    override fun onLoadFromJson(bundle: Map<String, String>): @EditEntity@? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: @EditEntity@? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, @EditEntity@.serializer(), entityJsonStr)
        }else {
            editEntity = @EditEntity@()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: @EditEntity@) {
        //TODO: Any validation that is needed before accepting / saving this entity
        //TODO: Only save to the database when the persistence mode is PERSISTENCE_MODE.DB
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.@Entity_VariableName@Uid == 0L) {
                entity.@Entity_VariableName@Uid = repo.@Entity_VariableName@Dao.insertAsync(entity)
            }else {
                repo.@Entity_VariableName@Dao.updateAsync(entity)
            }

            //TODO: Call commitToDatabase on any onetomany join helpers
            view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}