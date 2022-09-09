package com.ustadmobile.core.db

import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.lifecycle.Observer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull

suspend fun <T> waitForLiveData(liveData: LiveData<T>, timeout: Long, checker: (T) -> Boolean) {
    val channel = Channel<T>(1)
    val observerFn = Observer<T> { t ->
        if(checker.invoke(t))
            channel.trySend(t)
    }
    liveData.observeForever(observerFn)

    withTimeoutOrNull(timeout) { channel.receive() }

    liveData.removeObserver(observerFn)
    channel.close()
}

suspend fun <T> LiveData<T>.waitUntil(timeout: Long = 5000, checker: (T) -> Boolean): LiveData<T> {
    val completableDeferred = CompletableDeferred<T>()
    val observerFn = Observer<T> { t ->
        if(checker.invoke(t))
            completableDeferred.complete(t)
    }

    observeForever(observerFn)
    withTimeoutOrNull(timeout) { completableDeferred.await() }
    removeObserver(observerFn)

    return this
}

