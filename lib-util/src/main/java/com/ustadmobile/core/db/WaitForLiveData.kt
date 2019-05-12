package com.ustadmobile.core.db

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object WaitForLiveData {

    interface WaitForChecker<T> {
        fun done(value: T): Boolean
    }

    /**
     * Observer a given livedata source and wait for the specified period for the checker to return
     * true.
     *
     * @param liveData LiveData source
     * @param timeout timeout
     * @param timeoutUnit unit for timeout
     * @param checker interface to check for value
     * @param <T> The type of value returned by the live data
    </T> */
    fun <T> observeUntil(liveData: UmLiveData<T>, timeout: Long,
                         timeoutUnit: TimeUnit, checker: WaitForChecker<T>) {

        val latch = CountDownLatch(1)

        val observer = object : UmObserver<T> {
            override fun onChanged(t: T?) {
                if(t != null){
                    if (checker.done(t))
                        latch.countDown()
                }
            }
        }
        liveData.observeForever(observer)
        try {
            latch.await(timeout, timeoutUnit)
        } catch (e: InterruptedException) {
            //should not happen
        }

        liveData.removeObserver(observer)
    }


}
