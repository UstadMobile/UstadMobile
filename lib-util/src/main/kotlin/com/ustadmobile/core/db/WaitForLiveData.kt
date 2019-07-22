package com.ustadmobile.core.db

import com.ustadmobile.door.DoorLiveData
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

actual object WaitForLiveData {

    actual interface WaitForChecker<T> {
        actual fun done(value: T): Boolean
    }

    /**
     * Observer a given livedata source and wait for the specified period for the checker to return
     * true.
     *
     * @param liveData LiveData source
     * @param timeout timeout (in ms)
     * @param checker interface to check for value
     * @param <T> The type of value returned by the live data
    </T> */
    actual fun <T> observeUntil(liveData: DoorLiveData<T?>, timeout: Long, checker: WaitForChecker<T>) {

        val latch = CountDownLatch(1)

        val observer = { t: T? ->
            if (t != null) {
                if (checker.done(t))
                    latch.countDown()
            }
        }
        liveData.observeForever(observer)
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            //should not happen
        }

        liveData.removeObserver(observer)
    }


}