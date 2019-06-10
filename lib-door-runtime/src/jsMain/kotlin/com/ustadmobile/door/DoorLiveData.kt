package com.ustadmobile.door

actual abstract class DoorLiveData<T> {

    actual open fun observe(lifecycleOwner: DoorLifecycleOwner, observer: DoorObserver<in T>) {

    }

    actual open fun observeForever(observer: DoorObserver<in T>) {

    }

    actual open fun removeObserver(observer: DoorObserver<in T>) {

    }
}