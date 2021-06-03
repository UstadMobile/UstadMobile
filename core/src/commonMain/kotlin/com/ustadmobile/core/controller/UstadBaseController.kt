/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.impl.UmLifecycleListener
import com.ustadmobile.core.impl.UmLifecycleOwner
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.putResultDestInfo
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_KEY
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_VIEWNAME
import kotlinx.atomicfu.atomic
import kotlinx.serialization.SerializationStrategy
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import kotlin.js.JsName
import kotlin.reflect.KClass

/**
 * Base Controller that provides key functionality for any view :
 * it contains the data or a reference to the data and handles events and
 * logic.
 *
 * @author mike
 */
abstract class UstadBaseController<V : UstadView>(override val context: Any,
                                                  protected val arguments: Map<String, String>,
                                                  val view: V, override val di: DI): UmLifecycleOwner, DIAware {

    private val lifecycleListeners = mutableListOf<UmLifecycleListener>()

    private val lifecycleStatus = atomic(0)

    private var created: Boolean = false

    protected var savedState: Map<String, String>? = null
        private set

    protected val ustadNavController: UstadNavController by instance()

    /**
     * Handle when the presenter is created. Analogous to Android's onCreate
     *
     * @param savedState savedState if any
     */
    @JsName("onCreate")
    open fun onCreate(savedState: Map<String, String>?) {
        if(created) throw IllegalStateException("onCreate must be called ONCE AND ONLY ONCE! It has already been called")
        created = true
        this.savedState = savedState


        for (listener in lifecycleListeners) {
            listener.onLifecycleCreate(this)
        }


        lifecycleStatus.value = CREATED
    }

    /**
     * Handle when the presenter is about to become visible. Analogous to Android's onStart
     */
    open fun onStart() {

        for (listener in lifecycleListeners) {
            listener.onLifecycleStart(this)
        }


        lifecycleStatus.value = STARTED
    }

    /**
     * Handle when the presenter has become visible. Analogous to Android's onResume
     */
    @JsName("onResume")
    open fun onResume() {
        for (listener in lifecycleListeners) {
            listener.onLifecycleResume(this)
        }


        lifecycleStatus.value = RESUMED
    }

    open fun onPause() {
        for (listener in lifecycleListeners) {
            listener.onLifecyclePause(this)
        }


        lifecycleStatus.value = PAUSED
    }

    /**
     * Handle when the presenter is no longer visible. Analogous to Android's onStop
     */
    open fun onStop() {
        for (listener in lifecycleListeners) {
            listener.onLifecycleStop(this)
        }

        lifecycleStatus.value = STOPPED
    }

    /**
     * Called when the view is destroyed and removed from memory. Analogous to Android's onDestroy
     */
    open fun onDestroy() {
        for (listener in lifecycleListeners) {
            listener.onLifecycleDestroy(this)
        }


        lifecycleStatus.value = DESTROYED
    }

    override fun addLifecycleListener(listener: UmLifecycleListener) {
        lifecycleListeners.add(listener)

        when (lifecycleStatus.value) {
            CREATED -> listener.onLifecycleCreate(this)

            STARTED -> listener.onLifecycleStart(this)

            RESUMED -> listener.onLifecycleResume(this)

            PAUSED -> listener.onLifecyclePause(this)

            STOPPED -> listener.onLifecycleStop(this)

            DESTROYED -> listener.onLifecycleDestroy(this)
        }
    }

    override fun removeLifecycleListener(listener: UmLifecycleListener) {
        lifecycleListeners.remove(listener)
    }

    /**
     * This is used in roughly the same way as on Android. It can be called by the underlying
     * platform (e.g. Androids own onSaveInstanceState when the app is being destroyed) or
     * directly by the presenter e.g. to save the state of the entity before a user navigates away
     * to pick something.
     */
    open fun onSaveInstanceState(savedState: MutableMap<String, String>) {

    }

    /**
     * Save state to the nav controller savedStateHandle for the current back stack entry.
     * This will call onSaveInstanceState and save the resulting string map into the savedstatehandle.
     */
    open fun saveStateToNavController() {
        val stateMap = mutableMapOf<String, String>()
        onSaveInstanceState(stateMap)

        val stateHandle = ustadNavController.currentBackStackEntry?.savedStateHandle
        if(stateHandle != null) {
            stateMap.forEach {
                stateHandle.set(it.key, it.value)
            }
        }
    }


    /**
     * Save the result to the savedStateHandle on the backstack as directed by the current
     * arguments. ARG_RESULT_DEST_VIEWNAME and ARG_RESULT_DEST_KEY will specify which entry on
     * the backstack to lookup, and what key name should be used in the saved state handle once
     * it has been looked up.
     */
    protected fun finishWithResult(result: String) {
        val saveToViewName = arguments[ARG_RESULT_DEST_VIEWNAME]
        val saveToKey = arguments[ARG_RESULT_DEST_KEY]

        if(saveToViewName != null && saveToKey != null){
            val destBackStackEntry = ustadNavController.getBackStackEntry(saveToViewName)
            destBackStackEntry?.savedStateHandle?.set(saveToKey, result)

            ustadNavController.popBackStack(saveToViewName, false)
        }
    }


    fun requireSavedStateHandle(): UstadSavedStateHandle {
        return ustadNavController.currentBackStackEntry?.savedStateHandle
            ?: throw IllegalStateException("Require saved state handle: no current back stack entry")
    }

    /**
     * Navigate to an edit screen with the intent to return the result to the current screen.
     *
     */
    fun <T : Any> navigateToEditEntity(options: NavigateForResultOptions<T>) {

        saveStateToNavController()

        val currentBackStackEntryVal = ustadNavController.currentBackStackEntry
        val effectiveResultKey = options.destinationResultKey ?: options.entityClass.simpleName
            ?: throw IllegalArgumentException("navigateToEditEntity: no destination key and no class name")

        if(currentBackStackEntryVal != null) {
            options.arguments.putResultDestInfo(currentBackStackEntryVal, effectiveResultKey,
                options.overwriteDestination)
        }

        val currentEntityValue = options.currentEntityValue
        if(currentEntityValue != null) {
            options.arguments.put(
                UstadEditView.ARG_ENTITY_JSON,
                safeStringify(di, options.serializationStrategy, options.entityClass,
                    currentEntityValue))
        }

        ustadNavController.navigate(options.destinationViewName, options.arguments)
    }

    fun <T: Any> navigateToPickEntityFromList(destinationViewName: String,
                                              entityClass: KClass<T>,
                                              serializationStrategy: SerializationStrategy<T>,
                                              destinationResultKey: String? = null) {
        saveStateToNavController()

        val currentBackStackEntryVal = ustadNavController.currentBackStackEntry
        val effectiveResultKey = destinationResultKey ?: entityClass.simpleName
            ?: throw IllegalArgumentException("navigateToEditEntity: no destination key and no class name")



    }



    companion object {

        const val NOT_CREATED = 0

        const val CREATED = 1

        const val STARTED = 2

        const val RESUMED = 3

        const val PAUSED = 4

        const val STOPPED = 5

        const val DESTROYED = 6
    }

}
