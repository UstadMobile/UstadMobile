package com.ustadmobile.door

actual abstract class DoorLiveData<T> {

    private var value: T? = null

    private var initialValueLoaded = false

    private val observers = mutableListOf<DoorObserver<in T>>()

    actual constructor()

    constructor(value: T) {
        this.value = value
        initialValueLoaded = true
    }

    actual open fun observe(lifecycleOwner: DoorLifecycleOwner, observer: DoorObserver<in T>) {
        addActiveObserver(observer)
    }

    actual open fun observeForever(observer: DoorObserver<in T>) {
        addActiveObserver(observer)
    }

    actual open fun removeObserver(observer: DoorObserver<in T>) {
        val numObserversBefore = observers.size
        observers.remove(observer)
        if(numObserversBefore > 0 && observers.isEmpty()) {
            onInactive()
        }
    }

    private fun addActiveObserver(observer: DoorObserver<in T>) {
        observers.add(observer)
        if(observers.size == 1)
            onActive()
    }

    protected open fun onActive() {

    }

    protected open fun onInactive() {

    }

    protected fun postValue(value: T) {
        this.value = value
        initialValueLoaded = true
        observers.forEach { it.onChanged(value) }
    }

    actual open fun getValue() = value
}