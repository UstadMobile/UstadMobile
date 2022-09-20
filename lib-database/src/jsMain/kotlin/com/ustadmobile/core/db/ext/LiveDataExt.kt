package com.ustadmobile.core.db.ext

import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.lifecycle.Observer
import kotlinx.coroutines.CompletableDeferred

actual suspend  fun <T> LiveData<T>.getFirstValue(): T {
    val completable = CompletableDeferred<T>()

    val observer: Observer<T> = Observer {
        completable.complete(it)
    }
    try {
        observeForever(observer)
        return completable.await()
    }finally {
        removeObserver(observer)
    }

}