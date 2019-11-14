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
    /**
     * Function to run when the given lifecycleOwner becomes active
     */
    actual var onActive: (() -> Unit)?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    /**
     * Function to run when the given lifecycleOwner becomes inActive
     */
    actual var onInactive: (() -> Unit)?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    /**
     * Function to call to start actively observing the lifecycleOwner
     */
    actual fun addObserver() {
    }

    actual fun removeObserver() {
    }

}