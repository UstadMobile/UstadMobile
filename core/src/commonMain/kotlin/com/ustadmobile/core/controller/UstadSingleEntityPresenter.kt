package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadSingleEntityView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.doorMainDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.*

abstract class UstadSingleEntityPresenter<V: UstadSingleEntityView<RT>, RT: Any>(
        context: Any,
        arguments: Map<String, String>,
        view: V,
        val lifecycleOwner: DoorLifecycleOwner,
        override val di: DI): UstadBaseController<V>(context, arguments, view), DIAware {

    protected var entity: RT? = null

    enum class PersistenceMode{
        DB, JSON, LIVEDATA
    }

    abstract val persistenceMode: PersistenceMode

    var entityLiveData: DoorLiveData<RT?>? = null

    var entityLiveDataObserver: DoorObserver<RT?>? = null

    val systemImpl: UstadMobileSystemImpl by instance<UstadMobileSystemImpl>()

    val db: UmAppDatabase by instance<UmAppDatabase>(tag = TAG_DB)

    val repo: UmAppDatabase by instance<UmAppDatabase>(tag = TAG_REPO)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val mapWithEntityJson = if(savedState?.containsKey(ARG_ENTITY_JSON) == true) {
            savedState
        }else if(arguments.containsKey(ARG_ENTITY_JSON)){
            arguments
        }else {
            null
        }

        if(mapWithEntityJson != null && mapWithEntityJson[ARG_ENTITY_JSON] != null) {
            entity = onLoadFromJson(mapWithEntityJson)
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
        }else if(persistenceMode == PersistenceMode.LIVEDATA) {
            entityLiveData = onLoadLiveData(repo)
            view.loading = true
            if(entityLiveData != null) {
                entityLiveDataObserver = object : DoorObserver<RT?> {
                    override fun onChanged(t: RT?) {
                        view.entity = t
                        view.takeIf { t != null }?.loading = false
                    }
                }.also {
                    entityLiveData?.observe(lifecycleOwner, it)
                }
            }
        }
    }

    open suspend fun onLoadEntityFromDb(db: UmAppDatabase): RT? {
        return null
    }

    open fun onLoadLiveData(repo: UmAppDatabase): DoorLiveData<RT?>?{
        return null
    }

    open fun onLoadFromJson(bundle: Map<String, String>): RT? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        val entityLiveDataObserverVal = entityLiveDataObserver
        val entityLiveDataVal = entityLiveData
        if(entityLiveDataObserverVal != null && entityLiveDataVal != null) {
            entityLiveDataVal.removeObserver(entityLiveDataObserverVal)
        }
        entityLiveData = null
        entityLiveDataObserver = null
    }
}