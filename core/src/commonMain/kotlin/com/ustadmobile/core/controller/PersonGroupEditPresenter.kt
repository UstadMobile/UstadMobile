package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.PersonGroupEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.PersonGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class PersonGroupEditPresenter(context: Any,
                          arguments: Map<String, String>, view: PersonGroupEditView,
                          lifecycleOwner: LifecycleOwner,
                          di: DI)
    : UstadEditPresenter<PersonGroupEditView, PersonGroup>(context, arguments, view, di, lifecycleOwner) {

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

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): PersonGroup? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        //TODO: Load the list for any one to many join helper here
        /* e.g.
         val personGroup = withTimeoutOrNull {
             db.personGroup.findByUid(entityUid)
         } ?: PersonGroup()
         return personGroup
         */
        return TODO("Implement load from Database or return null if using PERSISTENCE_MODE.JSON")
    }

    override fun onLoadFromJson(bundle: Map<String, String>): PersonGroup? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: PersonGroup? = null
        editEntity = if(entityJsonStr != null) {
            safeParse(di, PersonGroup.serializer(), entityJsonStr)
        }else {
            PersonGroup()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, PersonGroup.serializer(), entity)
    }

    override fun handleClickSave(entity: PersonGroup) {
        //TODO: Any validation that is needed before accepting / saving this entity
        //TODO: Only save to the database when the persistence mode is PERSISTENCE_MODE.DB
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.groupUid == 0L) {
                entity.groupUid = repo.personGroupDao.insertAsync(entity)
            }else {
                repo.personGroupDao.updateAsync(entity)
            }

            //TODO: Call commitToDatabase on any onetomany join helpers
            finishWithResult(safeStringify(di, ListSerializer(PersonGroup.serializer()), listOf(entity)))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}