package com.ustadmobile.door

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

abstract class DoorLiveDataJdbcImpl<T>(val db: DoorDatabase, val tableNames: List<String>,
                                       val fetchFn: () -> T?): DoorLiveData<T>() {

    var value: T? = null

    private val activeObservers = CopyOnWriteArrayList<DoorObserver<in T?>>()

    private val lastUpdated = AtomicLong()

    private val lastChanged = AtomicLong(1)

    private val dbChangeListenerRequest = DoorDatabase.ChangeListenerRequest(tableNames) {
        lastUpdated.set(System.currentTimeMillis())
        update()
    }

    inner class LifecycleObserver(val observer: DoorObserver<in T?>): DoorLifecycleObserver() {

        override fun onStart(owner: DoorLifecycleOwner) {
            addActiveObserver(observer)
        }

        override fun onStop(owner: DoorLifecycleOwner) {
            removeActiveObserver(observer)
        }
    }


    override fun observe(lifecycleOwner: DoorLifecycleOwner, observer: DoorObserver<in T?>) {
        if(lifecycleOwner.currentState >= DoorLifecycleObserver.STARTED) {
            addActiveObserver(observer)
        }

        //TODO: Start listening to it's lifecycle
    }

    override fun observeForever(observer: DoorObserver<in T?>) {
        super.observeForever(observer)
    }

    override fun removeObserver(observer: DoorObserver<in T?>) {
        removeActiveObserver(observer)
    }


    private fun addActiveObserver(observer: DoorObserver<in T?>) {
        activeObservers.add(observer)

        if(activeObservers.size > 1 && lastUpdated.get() > lastChanged.get()) {
            observer.onChanged(value)
        }else {
            db.addChangeListener(dbChangeListenerRequest)
        }
    }

    private fun removeActiveObserver(observer: DoorObserver<in T?>) {
        if(activeObservers.remove(observer) && activeObservers.isEmpty()) {
            db.removeChangeListener(dbChangeListenerRequest)
        }
    }

    private fun update() {
        GlobalScope.launch {
            val newVal = fetchFn()
            lastUpdated.set(System.currentTimeMillis())
            activeObservers.forEach { it.onChanged(newVal) }
        }
    }


}