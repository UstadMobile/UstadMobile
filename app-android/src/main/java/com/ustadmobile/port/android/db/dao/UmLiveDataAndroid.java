package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Handler;
import android.os.Looper;

import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;

import java.util.HashMap;

/**
 * Created by mike on 1/14/18.
 */

public class UmLiveDataAndroid<T> implements UmLiveData<T> {

    private LiveData<T> src;

    private HashMap<UmObserver<T>, Observer<T>> observersHashMap;

    public UmLiveDataAndroid(LiveData<T> src) {
        this.src = src;
        observersHashMap = new HashMap<>();
    }

    @Override
    public T getValue() {
        return src.getValue();
    }

    @Override
    public void observe(UstadController controller, UmObserver<T> observer) {
        LifecycleOwner owner = (LifecycleOwner)controller.getContext();
        Observer<T> observerImpl = observer::onChanged;
        src.observe(owner, observerImpl);
        observersHashMap.put(observer, observerImpl);
    }

    @Override
    public void observeForever(UmObserver<T> observer) {
        Observer<T> observerImpl = observer::onChanged;
        src.observeForever(observerImpl);
        observersHashMap.put(observer, observerImpl);
    }

    @Override
    public void removeObserver(UmObserver<T> observer) {
        Observer<T> observerImpl = observersHashMap.get(observer);
        if(observerImpl == null)
            return;

        Runnable removeObserverRunnable = () -> {
            synchronized (UmLiveDataAndroid.this){
                src.removeObserver(observerImpl);
                observersHashMap.remove(observer);
                notifyAll();
            }
        };

        if(Looper.myLooper() == Looper.getMainLooper()) {
            removeObserverRunnable.run();
        }else {
            synchronized (this) {
                new Handler(Looper.getMainLooper()).post(removeObserverRunnable);
                if(observersHashMap.containsKey(observer)){
                    try { wait(); }
                    catch(InterruptedException e) {}
                }
            }
        }
    }
}
