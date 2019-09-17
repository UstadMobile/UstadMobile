package com.ustadmobile.door

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class DoorLiveDataJs<T>(val fetchFn: suspend () -> T) : DoorLiveData<T>() {

    override fun observe(lifecycleOwner: DoorLifecycleOwner, observer: DoorObserver<in T>) {
        GlobalScope.async {
            observer.onChanged(fetchFn())
        }
    }

    override fun observeForever(observer: DoorObserver<in T>) {
        GlobalScope.async {
            observer.onChanged(fetchFn())
        }
    }
}