package com.ustadmobile.door

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

actual class RepositoryLoadHelperLifecycleHelper actual constructor(lifecycleOwner: DoorLifecycleOwner) : LifecycleObserver {

    val actLifecycleOwner = lifecycleOwner

    actual var onActive: (() -> Unit)? = null

    actual var onInactive: (() -> Unit)? = null

    actual fun addObserver() {
        actLifecycleOwner.lifecycle.addObserver(this)
    }

    actual fun removeObserver() {
        actLifecycleOwner.lifecycle.removeObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        onActive?.invoke()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        onInactive?.invoke()
    }

}