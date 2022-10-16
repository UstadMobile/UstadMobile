package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_ID
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_VIEWNAME
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.util.copyOnWriteListOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

abstract class UstadEditPresenter<V: UstadEditView<RT>, RT: Any>(
    context: Any,
    arguments: Map<String, String>,
    view: V,
    di: DI,
    lifecycleOwner: LifecycleOwner,
    override val activeSessionRequired: Boolean = true
) : UstadSingleEntityPresenter<V, RT>(context, arguments, view, di, lifecycleOwner), UstadEditPresenterJsonLoader {

    private val jsonLoadListeners: MutableList<JsonLoadListener> = copyOnWriteListOf()

    interface JsonLoadListener {

        fun onLoadFromJsonSavedState(savedState: Map<String, String>?)

        fun onSaveState(outState: MutableMap<String, String>)

    }

    abstract fun handleClickSave(entity: RT)

    override fun addJsonLoadListener(loadListener: JsonLoadListener) = jsonLoadListeners.add(loadListener)

    override fun removeJsonLoadListener(loadListener: JsonLoadListener) = jsonLoadListeners.remove(loadListener)

    fun requireBackStackEntry() = requireNavController().currentBackStackEntry ?: throw IllegalStateException("requirebackstackentry: no currentbackstackentry!")

    override fun onLoadFromJson(bundle: Map<String, String>): RT? {
        jsonLoadListeners.forEach { it.onLoadFromJsonSavedState(bundle) }
        return super.onLoadFromJson(bundle)
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        jsonLoadListeners.forEach { it.onSaveState(savedState) }
        super.onSaveInstanceState(savedState)
    }

    protected val isExistingEntityOrPickerMode
        get() = (arguments[ARG_ENTITY_UID]?.toLong() ?: 0L) != 0L ||
                arguments[ARG_RESULT_DEST_ID] != null ||
                arguments[ARG_RESULT_DEST_VIEWNAME] != null

    fun onFinish(detailViewName: String, entityUid: Long, entity: RT, serializer: KSerializer<RT>) {
        if(!isExistingEntityOrPickerMode) {
            systemImpl.go(detailViewName,
                mapOf(ARG_ENTITY_UID to entityUid.toString()), context,
                UstadMobileSystemCommon.UstadGoOptions(CURRENT_DEST, popUpToInclusive = true))
        }  else {
            finishWithResult(safeStringify(di, ListSerializer(serializer), listOf(entity)))
        }
    }
}