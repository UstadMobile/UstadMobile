package com.ustadmobile.door


actual class RepositoryLoadHelperLifecycleHelper actual constructor(lifecycleOwner: DoorLifecycleOwner) {
    actual var onActive: (() -> Unit)? = null

    actual var onInactive: (() -> Unit)? = null

    actual fun addObserver() {
        //Not implemented on JVM
    }

    actual fun removeObserver() {
        //Not implemented on JVM
    }

}