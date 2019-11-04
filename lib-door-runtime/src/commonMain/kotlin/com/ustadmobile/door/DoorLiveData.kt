package com.ustadmobile.door

import kotlinx.coroutines.CompletableDeferred

/**
 * Suspend function that will wait for the first onChanged call to the observerm and then returns
 * the value.
 */
suspend fun <T> DoorLiveData<T>.getFirstValue(): T {
    val completableDeferred = CompletableDeferred<T>()

    val tmpObserver = object: DoorObserver<T> {
        override fun onChanged(t: T) {
            completableDeferred.complete(t)
        }
    }

    this.observeForever(tmpObserver)
    completableDeferred.await()
    this.removeObserver(tmpObserver)
    return completableDeferred.getCompleted()
}

expect abstract class DoorLiveData<T>() {

    open fun observe(lifecycleOwner: DoorLifecycleOwner, observer: DoorObserver<in T>)

    open fun observeForever(observer: DoorObserver<in T>)

    open fun removeObserver(observer: DoorObserver<in T>)

}