package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.@Entity@DetailView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.@Entity@
@DisplayEntity_Import@
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.kodein.di.DI


class @Entity@DetailPresenter(context: Any,
        arguments: Map<String, String>, view: @BaseFileName@View,
        lifecycleOwner: LifecycleOwner,
        di: DI)
    : UstadDetailPresenter<@Entity@DetailView, @DisplayEntity@>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = TODO("PersistenceMode.DB, PersistenceMode.JSON, or PersistenceMode.LIVEDATA")

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): @DisplayEntity@? {
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


    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}