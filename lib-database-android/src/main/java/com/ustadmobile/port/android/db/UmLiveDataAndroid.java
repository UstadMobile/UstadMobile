//package com.ustadmobile.port.android.db;
//
//import android.os.Handler;
//import android.os.Looper;
//
//import androidx.lifecycle.LifecycleOwner;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.Observer;
//
//import com.ustadmobile.door.DoorLiveDataKt;
//
//import java.util.HashMap;
//
///**
// * Created by mike on 1/14/18.
// */
//
//public class UmLiveDataAndroid<T> extends DoorLiveDataKt<T> {
//
//    private LiveData<T> src;
//
//    private HashMap<UmObserver<T>, Observer<T>> observersHashMap;
//
//    public UmLiveDataAndroid(LiveData<T> src) {
//        this.src = src;
//        observersHashMap = new HashMap<>();
//    }
//
//    @Override
//    public T getValue() {
//        return src.getValue();
//    }
//
//    @Override
//    public void observe(UmLifecycleOwner controller, UmObserver<T> observer) {
//        Observer<T> observerImpl = observer::onChanged;
//        if(controller.getContext() instanceof LifecycleOwner) {
//            LifecycleOwner owner = (LifecycleOwner)controller.getContext();
//            src.observe(owner, observerImpl);
//        }else {
//            src.observeForever(observerImpl);
//        }
//
//        observersHashMap.put(observer, observerImpl);
//    }
//
//    @Override
//    public void observeForever(UmObserver<T> observer) {
//        Observer<T> observerImpl = observer::onChanged;
//        src.observeForever(observerImpl);
//        observersHashMap.put(observer, observerImpl);
//    }
//
//    @Override
//    public void removeObserver(UmObserver<T> observer) {
//        Observer<T> observerImpl = observersHashMap.get(observer);
//        if(observerImpl == null)
//            return;
//
//        Runnable removeObserverRunnable = () -> {
//            synchronized (UmLiveDataAndroid.this){
//                src.removeObserver(observerImpl);
//                observersHashMap.remove(observer);
//                notifyAll();
//            }
//        };
//
//        if(Looper.myLooper() == Looper.getMainLooper()) {
//            removeObserverRunnable.run();
//        }else {
//            synchronized (this) {
//                new Handler(Looper.getMainLooper()).post(removeObserverRunnable);
//                if(observersHashMap.containsKey(observer)){
//                    try { wait(); }
//                    catch(InterruptedException e) {}
//                }
//            }
//        }
//    }
//}
