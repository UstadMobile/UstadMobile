package com.ustadmobile.port.android.netwokmanager

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.WorkManager
import com.ustadmobile.port.android.sync.UmAppDatabaseSyncWorker
import java.util.concurrent.TimeUnit


/**
 * This service schedules the first WorkManager job for the sync, and then tracks if the app is
 * in the foreground (or not).
 */
class UmAppDatabaseSyncService : Service(), LifecycleObserver {

    private val mBinder = LocalServiceBinder()

    inner class LocalServiceBinder : Binder() {

        val service: UmAppDatabaseSyncService
            get() = this@UmAppDatabaseSyncService
    }

    override fun onCreate() {
        super.onCreate()
        isInForeground = true
        WorkManager.getInstance().cancelAllWorkByTag(UmAppDatabaseSyncWorker.TAG)

        UmAppDatabaseSyncWorker.queueSyncWorker(100, TimeUnit.MILLISECONDS);
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        isInForeground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        isInForeground = false
        lastForegroundTime = System.currentTimeMillis()
    }

    companion object {

        @Volatile
        var isInForeground = false
            private set

        val SYNC_AFTER_BACKGROUND_LAG = (5 * 60 * 1000).toLong()

        @Volatile
        var lastForegroundTime = 0L
            private set
    }


}
