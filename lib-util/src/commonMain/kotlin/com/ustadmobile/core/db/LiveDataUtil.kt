package com.ustadmobile.core.db

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull

suspend fun <T> waitForLiveData(liveData: DoorLiveData<T>, timeout: Long, checker: (T) -> Boolean) {
    val channel = Channel<T>(1)
    val observerFn = object : DoorObserver<T> {
        override fun onChanged(t: T) {
            if(checker.invoke(t))
                channel.trySend(t)
        }
    }
    liveData.observeForever(observerFn)

    withTimeoutOrNull(timeout) { channel.receive() }

    liveData.removeObserver(observerFn)
    channel.close()
}

suspend fun <T> DoorLiveData<T>.waitUntil(timeout: Long = 5000, checker: (T) -> Boolean): DoorLiveData<T> {
    val completableDeferred = CompletableDeferred<T>()
    val observerFn = object : DoorObserver<T> {
        override fun onChanged(t: T) {
            if(checker.invoke(t))
                completableDeferred.complete(t)
        }
    }

    observeForever(observerFn)
    withTimeoutOrNull(timeout) { completableDeferred.await() }
    removeObserver(observerFn)

    return this
}

