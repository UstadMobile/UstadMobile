package com.ustadmobile.door

fun <T: Any> doorObserve(liveData: DoorLiveData<T>, lifecycleOwner: Any, observer:(T) -> Unit) {
    liveData.observe(lifecycleOwner as DoorLifecycleOwner, object : DoorObserver<T> {
        override fun onChanged(t: T) {
            observer.invoke(t)
        }
    })
}

