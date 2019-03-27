package com.ustadmobile.core.db;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WaitForLiveData {

    public interface WaitForChecker<T> {
        boolean done(T value);
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
     */
    public static <T> void observeUntil(UmLiveData<T> liveData, long timeout,
                                        TimeUnit timeoutUnit, WaitForChecker<T> checker) {

        CountDownLatch latch = new CountDownLatch(1);

        UmObserver<T> observer = (newVal) -> {
            if(checker.done(newVal))
                latch.countDown();
        };
        liveData.observeForever(observer);
        try { latch.await(timeout, timeoutUnit); }
        catch(InterruptedException e) {
            //should not happen
        }

        liveData.removeObserver(observer);
    }


}
