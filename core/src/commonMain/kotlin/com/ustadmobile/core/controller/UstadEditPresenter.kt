package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class UstadEditPresenter<V: UstadEditView<RT>, RT>(context: Any,
                                           arguments: Map<String, String>, view: V,
                                           val lifecycleOwner: DoorLifecycleOwner,
                                           val systemImpl: UstadMobileSystemImpl,
                                           val db: UmAppDatabase, val repo: UmAppDatabase,
                                           val activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadBaseController<V>(context, arguments, view) {

    enum class PERSISTENCE_MODE{
        DB, JSON
    }

    abstract val persistenceMode: PERSISTENCE_MODE

    protected var entity: RT? = null

    abstract fun handleClickSave(entity: RT)

    open suspend fun onLoadEntityFromDb(db: UmAppDatabase): RT? {
        return null
    }

    open fun onLoadFromJson(bundle: Map<String, String>): RT? {
        return null
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        if(savedState != null) {
            entity = onLoadFromJson(savedState)
            view.entity = entity
        }else if(persistenceMode == PERSISTENCE_MODE.DB) {
            view.loading = true
            view.fieldsEnabled = false
            GlobalScope.launch(doorMainDispatcher()) {
                listOf(db, repo).forEach {
                    entity = onLoadEntityFromDb(it)
                    view.entity = entity
                }

                view.loading = false
                view.fieldsEnabled = true
            }
        }else if(persistenceMode == PERSISTENCE_MODE.JSON){
            entity = onLoadFromJson(arguments)
            view.entity = entity
        }
    }

    companion object {
        const val ARG_SAVEDSTATE_ENTITY = "entity"
    }
}