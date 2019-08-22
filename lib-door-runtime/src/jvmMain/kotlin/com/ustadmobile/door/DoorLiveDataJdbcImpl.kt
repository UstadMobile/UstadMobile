package com.ustadmobile.door

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class DoorLiveDataJdbcImpl<T>(val db: DoorDatabase, val tableNames: List<String>,
                                       val fetchFn: () -> T): DoorLiveData<T>() {


    var value = AtomicReference<T>()

    private val activeObservers = CopyOnWriteArrayList<DoorObserver<in T>>()

    private val lastUpdated = AtomicLong()

    private val lastChanged = AtomicLong(1)

    private val dbChangeListenerRequest = DoorDatabase.ChangeListenerRequest(tableNames) {
        lastUpdated.set(System.currentTimeMillis())
        update()
    }

    inner class LifecycleObserver(val observer: DoorObserver<in T>): DoorLifecycleObserver() {

        override fun onStart(owner: DoorLifecycleOwner) {
            addActiveObserver(observer)
        }

        override fun onStop(owner: DoorLifecycleOwner) {
            removeActiveObserver(observer)
        }
    }


    override fun observe(lifecycleOwner: DoorLifecycleOwner, observer: DoorObserver<in T>) {
        if(lifecycleOwner.currentState >= DoorLifecycleObserver.STARTED) {
            addActiveObserver(observer)
        }

        //TODO: Start listening to it's lifecycle
    }

    override fun observeForever(observer: DoorObserver<in T>) {
        addActiveObserver(observer)
    }

    override fun removeObserver(observer: DoorObserver<in T>) {
        removeActiveObserver(observer)
    }


    private fun addActiveObserver(observer: DoorObserver<in T>) {
        activeObservers.add(observer)

        if(activeObservers.size > 1 && lastUpdated.get() > lastChanged.get()) {
            observer.onChanged(value.get())
        }else {
            db.addChangeListener(dbChangeListenerRequest)
            update()
        }
    }

    private fun removeActiveObserver(observer: DoorObserver<in T>) {
        if(activeObservers.remove(observer) && activeObservers.isEmpty()) {
            db.removeChangeListener(dbChangeListenerRequest)
        }
    }

    private fun update() {
        GlobalScope.launch {
            val retVal = fetchFn()
            this@DoorLiveDataJdbcImpl.value.set(retVal)
            lastUpdated.set(System.currentTimeMillis())
            activeObservers.forEach { it.onChanged(retVal) }
        }
    }


}