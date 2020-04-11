package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadSingleEntityView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class UstadSingleEntityPresenter<V: UstadSingleEntityView<RT>, RT>(
        context: Any,
        arguments: Map<String, String>,
        view: V,
        val lifecycleOwner: DoorLifecycleOwner,
        val systemImpl: UstadMobileSystemImpl,
        val db: UmAppDatabase, val repo: UmAppDatabase,
        val activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData): UstadBaseController<V>(context, arguments, view) {

    protected var entity: RT? = null

    enum class PersistenceMode{
        DB, JSON, LIVEDATA
    }

    abstract val persistenceMode: PersistenceMode


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        if(savedState != null && savedState[ARG_ENTITY_JSON] != null) {
            entity = onLoadFromJson(savedState)
            view.entity = entity
            (view as? UstadEditView<*>)?.fieldsEnabled = true
        }else if(persistenceMode == PersistenceMode.DB) {
            view.loading = true
            (view as? UstadEditView<*>)?.fieldsEnabled = false
            GlobalScope.launch(doorMainDispatcher()) {
                listOf(db, repo).forEach {
                    entity = onLoadEntityFromDb(it)
                    view.entity = entity
                }

                view.loading = false
                (view as? UstadEditView<*>)?.fieldsEnabled = true
            }
        }else if(persistenceMode == PersistenceMode.JSON){
            entity = onLoadFromJson(arguments)
            view.entity = entity
        }
    }

    open suspend fun onLoadEntityFromDb(db: UmAppDatabase): RT? {
        return null
    }

    open fun onLoadLiveData(repo: UmAppDatabase): RT?{
        return null
    }

    open fun onLoadFromJson(bundle: Map<String, String>): RT? {
        return null
    }


}