package com.ustadmobile.door

/**
 * This expect/actual is used by RepositoryLoadHelper to monitor the lifecycle of anything which is
 * observing it's related LiveData.  This allows the RepositoryLoadHelper to load automatically when
 * there are pending requests where the results of which are being actively observed, and avoid
 * attempting to reload data that is not being actively observed.
 *
 * The RepositoryLoadHelper must call addObserver to start observing the lifecycleOwner and
 * removeObserver to stop observing the given lifecycleOwner.
 */
actual class RepositoryLoadHelperLifecycleHelper actual constructor(lifecycleOwner: DoorLifecycleOwner) {

    var actLifecycleOwner: DoorLifecycleOwner? = lifecycleOwner

    actual var onActive: (() -> Unit)? = null

    actual var onInactive: (() -> Unit)? = null

    actual var onDestroyed: (() -> Unit)? = null

    /**
     * Function to call to start actively observing the lifecycleOwner
     */
    actual fun addObserver() {
    }

    actual fun removeObserver() {
    }

    /**
     * Returns the current state as an Int as per DoorLifecycleObserver constants
     */
    actual val currentState: Int
        get() = if(actLifecycleOwner != null) DoorLifecycleObserver.RESUMED else DoorLifecycleObserver.DESTROYED

    actual fun dispose() {
        actLifecycleOwner = null
    }

}