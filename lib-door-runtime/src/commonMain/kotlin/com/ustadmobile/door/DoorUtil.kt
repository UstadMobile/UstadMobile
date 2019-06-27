package com.ustadmobile.door

fun <T: Any?> DoorLiveData<T>.observe(lifecycleOwner: DoorLifecycleOwner, observer: (T?) -> Unit) {
    this.observe(lifecycleOwner, object : DoorObserver<T?> {
        override fun onChanged(t: T?) {
            observer.invoke(t)
        }
    })
}

class ObserverFnWrapper<T>(val observerFn: (T) -> Unit): DoorObserver<T> {
    override fun onChanged(t: T) {
        observerFn.invoke(t)
    }
}
