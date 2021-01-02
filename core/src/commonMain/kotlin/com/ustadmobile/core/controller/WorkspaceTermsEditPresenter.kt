package com.ustadmobile.core.controller

import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.WorkspaceTermsEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI


class WorkspaceTermsEditPresenter(context: Any,
        arguments: Map<String, String>, view: WorkspaceTermsEditView,
        lifecycleOwner: DoorLifecycleOwner,
        di: DI)
    : UstadEditPresenter<WorkspaceTermsEditView, SiteTermsWithLanguage>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override fun onLoadFromJson(bundle: Map<String, String>): SiteTermsWithLanguage? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SiteTermsWithLanguage? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, SiteTermsWithLanguage.serializer(), entityJsonStr)
        }else {
            editEntity = SiteTermsWithLanguage()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: SiteTermsWithLanguage) {
        //TODO: Any validation that is needed before accepting / saving this entity
        //TODO: Only save to the database when the persistence mode is PERSISTENCE_MODE.DB
        GlobalScope.launch(doorMainDispatcher()) {

            //TODO: Call commitToDatabase on any onetomany join helpers
            view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}