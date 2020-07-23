package com.ustadmobile.core.controller

import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.util.copyOnWriteListOf
import org.kodein.di.DI

abstract class UstadEditPresenter<V: UstadEditView<RT>, RT: Any>(context: Any,
    arguments: Map<String, String>, view: V, di: DI, lifecycleOwner: DoorLifecycleOwner)

    : UstadSingleEntityPresenter<V, RT>(context, arguments, view, di, lifecycleOwner) {

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