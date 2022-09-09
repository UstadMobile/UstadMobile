package com.ustadmobile.core.db.ext

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend  fun <T> LiveData<T>.getFirstValue(): T {
    val completable = CompletableDeferred<T>()

    val observer: Observer<T> = Observer {
        completable.complete(it)
    }

    try {
        withContext(Dispatchers.Main) {
            observeForever(observer)
        }

        return completable.await()
    }finally {
        withContext(Dispatchers.Main) {
            removeObserver(observer)
        }

    }

}