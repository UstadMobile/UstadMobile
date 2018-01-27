package com.ustadmobile.core.fs.db;

import com.ustadmobile.core.db.UmObserver;

/**
 * Created by mike on 1/26/18.
 */

public class NotifyUmObserer<T> implements UmObserver<T> {

    private T value;

    public NotifyUmObserer() {
    }

    @Override
    public synchronized void onChanged(T t) {
        this.value = t;
        notifyAll();
    }

    public synchronized void waitForOnChanged() {
        try { wait(); }
        catch(InterruptedException e) {}
    }

    public T getValue() {
        return value;
    }

}
