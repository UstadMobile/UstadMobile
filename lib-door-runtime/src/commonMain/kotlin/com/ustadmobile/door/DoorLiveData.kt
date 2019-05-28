package com.ustadmobile.door

fun <T: Any> doorObserve(liveData: DoorLiveData<T>, lifecycleOwner: Any, observer:(T) -> Unit) {
    liveData.observe(lifecycleOwner as DoorLifecycleOwner, object : DoorObserver<T> {
        override fun onChanged(t: T) {
            observer.invoke(t)
        }
    })
}

expect abstract class DoorLiveData<T>() {

    open fun observe(lifecycleOwner: DoorLifecycleOwner, observer: DoorObserver<in T>)

    open fun observeForever(observer: DoorObserver<in T>)

    open fun removeObserver(observer: DoorObserver<in T>)

}