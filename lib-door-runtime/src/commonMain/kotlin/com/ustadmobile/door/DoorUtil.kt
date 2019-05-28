package com.ustadmobile.door

fun <T: Any?> DoorLiveData<T>.observe(lifecycleOwner: Any, observer: (T) -> Unit) {
    this.observe(lifecycleOwner as DoorLifecycleOwner, object : DoorObserver<T> {
        override fun onChanged(t: T) {
            observer.invoke(t)
        }
    })
}
