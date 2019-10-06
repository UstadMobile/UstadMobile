package com.ustadmobile.core.db

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull

suspend fun <T> waitForLiveData(liveData: DoorLiveData<T>, timeout: Long, checker: (T) -> Boolean) {
    val channel = Channel<T>(1)
    val observerFn = object : DoorObserver<T> {
        override fun onChanged(t: T) {
            if(checker.invoke(t))
                channel.offer(t)
        }
    }
    liveData.observeForever(observerFn)

    withTimeoutOrNull(timeout) { channel.receive() }

    liveData.removeObserver(observerFn)
    channel.close()
}