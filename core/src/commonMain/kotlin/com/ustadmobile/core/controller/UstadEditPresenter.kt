package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.copyOnWriteListOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class UstadEditPresenter<V: UstadEditView<RT>, RT: Any>(context: Any,
    arguments: Map<String, String>, view: V,
    lifecycleOwner: DoorLifecycleOwner,
    systemImpl: UstadMobileSystemImpl,
    db: UmAppDatabase,
    repo: UmAppDatabase,
    activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadSingleEntityPresenter<V, RT>(context, arguments, view, lifecycleOwner, systemImpl, db, repo, activeAccount) {

    private val jsonLoadListeners: MutableList<JsonLoadListener> = copyOnWriteListOf()

    interface JsonLoadListener {

        fun onLoadFromJsonSavedState(savedState: Map<String, String>?)

        fun onSaveState(outState: MutableMap<String, String>)

    }

    abstract fun handleClickSave(entity: RT)

    fun addJsonLoadListener(loadListener: JsonLoadListener) = jsonLoadListeners.add(loadListener)

    fun removeJsonLoadListener(loadListener: JsonLoadListener) = jsonLoadListeners.remove(loadListener)

    override fun onLoadFromJson(bundle: Map<String, String>): RT? {
        jsonLoadListeners.forEach { it.onLoadFromJsonSavedState(bundle) }
        return super.onLoadFromJson(bundle)
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        jsonLoadListeners.forEach { it.onSaveState(savedState) }
        super.onSaveInstanceState(savedState)
    }
}