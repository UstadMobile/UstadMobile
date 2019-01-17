package com.ustadmobile.port.android.netwokmanager;

import android.app.Service;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ustadmobile.port.android.sync.UmAppDatabaseSyncWorker;

import java.util.concurrent.TimeUnit;

import androidx.work.WorkManager;

/**
 * This service schedules the first WorkManager job for the sync, and then tracks if the app is
 * in the foreground (or not).
 */
public class UmAppDatabaseSyncService extends Service implements LifecycleObserver {

    private static volatile boolean inForeground = false;

    public static final long SYNC_AFTER_BACKGROUND_LAG = 5 * 60 * 1000;

    private static volatile long lastForegroundTime = 0L;

    private final IBinder mBinder = new LocalServiceBinder();

    public class LocalServiceBinder extends Binder {

        public UmAppDatabaseSyncService getService() {
            return UmAppDatabaseSyncService.this;
        }
    }


    public UmAppDatabaseSyncService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        inForeground = true;
        WorkManager.getInstance().cancelAllWorkByTag(UmAppDatabaseSyncWorker.TAG);
        UmAppDatabaseSyncWorker.queueSyncWorker(100, TimeUnit.MILLISECONDS);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public static boolean isInForeground() {
        return inForeground;
    }

    public static long getLastForegroundTime() {
        return lastForegroundTime;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        inForeground = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        inForeground = false;
        lastForegroundTime = System.currentTimeMillis();
    }


}
