package com.ustadmobile.lib.db;

import com.ustadmobile.core.impl.UmCallback;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceCallback<T> implements UmCallback<T> {

    private AtomicReference<T> atomicReference;

    private Throwable exception;

    private CountDownLatch latch;

    public AtomicReferenceCallback() {
        atomicReference = new AtomicReference<>();
        latch = new CountDownLatch(1);
    }

    @Override
    public void onSuccess(T result) {
        atomicReference.set(result);
        latch.countDown();
    }

    @Override
    public void onFailure(Throwable exception) {
        this.exception = exception;
    }

    public T getResult(long time, TimeUnit timeUnit) {
        if(exception != null)
            throw new RuntimeException("OnFailure called", exception);

        try { latch.await(time, timeUnit); }
        catch(InterruptedException e) {
            //should not happen
        }

        return atomicReference.get();
    }

}
