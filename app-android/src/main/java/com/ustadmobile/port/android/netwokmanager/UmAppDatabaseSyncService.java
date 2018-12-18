package com.ustadmobile.port.android.netwokmanager;

import android.app.Service;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.UmAccount;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This service calls the database sync periodically whilst the app is in the foreground, or was
 * recently in the foreground. It is bound to UmBaseActivity.
 */
public class UmAppDatabaseSyncService extends Service implements LifecycleObserver {

    private ScheduledExecutorService syncExecutor;

    private int MAX_INTERVAL = 60*1000;

    private int MIN_INTERVAL = 10*1000;

    boolean inForeground = true;

    private long SYNC_AFTER_BACKGROUND_LAG = 5 * 60 * 1000;

    private long lastForegroundTime = 0L;

    private final IBinder mBinder = new LocalServiceBinder();

    public class LocalServiceBinder extends Binder {

        public UmAppDatabaseSyncService getService() {
            return UmAppDatabaseSyncService.this;
        }
    }

    private class SyncTimerTask extends TimerTask {
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            if(inForeground
                    || (System.currentTimeMillis() - lastForegroundTime) < SYNC_AFTER_BACKGROUND_LAG) {
                Context context = getApplicationContext();
                UmAccount activeAccount = UmAccountManager.getActiveAccount(getApplicationContext());

                try {
                    UmAppDatabase.getInstance(context).syncWith(
                            UmAccountManager.getRepositoryForActiveAccount(context),
                            activeAccount != null ? activeAccount.getPersonUid() : 0, 100, 100);
                    UstadMobileSystemImpl.l(UMLog.INFO, 100, "database syncWith repo ran");
                }catch(Exception e) {
                    UstadMobileSystemImpl.l(UMLog.WARN, 101, "Exception running syncWith :" +
                            e.getMessage());
                }
            }
            long timeToNextRun = (startTime + MAX_INTERVAL) - System.currentTimeMillis();

            synchronized (UmAppDatabaseSyncService.this) {
                if(!syncExecutor.isShutdown())
                    syncExecutor.schedule(new SyncTimerTask(), Math.max(MIN_INTERVAL, timeToNextRun),
                            TimeUnit.MILLISECONDS);

            }

        }
    }

    public UmAppDatabaseSyncService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        syncExecutor = Executors.newSingleThreadScheduledExecutor();
        syncExecutor.schedule(new SyncTimerTask(), MIN_INTERVAL, TimeUnit.MILLISECONDS);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        synchronized (this) {
            syncExecutor.shutdownNow();
        }

        ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // not supported
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
