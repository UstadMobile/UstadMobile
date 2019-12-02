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

import com.ustadmobile.core.impl.UmLifecycleListener
import com.ustadmobile.core.impl.UmLifecycleOwner
import com.ustadmobile.core.view.UstadView
import kotlinx.atomicfu.atomic
import kotlin.js.JsName

/**
 * Base Controller that provides key functionality for any view :
 * it contains the data or a reference to the data and handles events and
 * logic.
 *
 * @author mike
 */
abstract class UstadBaseController<V : UstadView>(override val context: Any,
                                                  val arguments: Map<String, String?>, val view: V)
    : UmLifecycleOwner {

    private val lifecycleListeners = mutableListOf<UmLifecycleListener>()

    private val lifecycleStatus = atomic(0)

    /**
     * Handle when the presenter is created. Analogous to Android's onCreate
     *
     * @param savedState savedState if any
     */
    @JsName("onCreate")
    open fun onCreate(savedState: Map<String, String?>?) {
        synchronized(lifecycleListeners) {
            for (listener in lifecycleListeners) {
                listener.onLifecycleCreate(this)
            }
        }

        lifecycleStatus.value = CREATED
    }

    /**
     * Handle when the presenter is about to become visible. Analogous to Android's onStart
     */
    fun onStart() {
        synchronized(lifecycleListeners) {
            for (listener in lifecycleListeners) {
                listener.onLifecycleStart(this)
            }
        }

        lifecycleStatus.value = STARTED
    }

    /**
     * Handle when the presenter has become visible. Analogous to Android's onResume
     */
    @JsName("onResume")
    open fun onResume() {
        synchronized(lifecycleListeners) {
            for (listener in lifecycleListeners) {
                listener.onLifecycleResume(this)
            }
        }

        lifecycleStatus.value = RESUMED
    }

    /**
     * Handle when the presenter is no longer visible. Analogous to Android's onStop
     */
    fun onStop() {
        synchronized(lifecycleListeners) {
            for (listener in lifecycleListeners) {
                listener.onLifecycleStop(this)
            }
        }

        lifecycleStatus.value = STOPPED
    }

    /**
     * Called when the view is destroyed and removed from memory. Analogous to Android's onDestroy
     */
    open fun onDestroy() {
        synchronized(lifecycleListeners) {
            for (listener in lifecycleListeners) {
                listener.onLifecycleDestroy(this)
            }
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
