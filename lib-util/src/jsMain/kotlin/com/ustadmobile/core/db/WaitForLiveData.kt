package com.ustadmobile.core.db

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
    actual fun <T> observeUntil(liveData: UmLiveData<T>, timeout: Long, checker: WaitForChecker<T>) {
    }


}