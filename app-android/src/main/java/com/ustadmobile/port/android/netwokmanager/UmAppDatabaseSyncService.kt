package com.ustadmobile.port.android.netwokmanager

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.*
import androidx.work.WorkManager
import com.ustadmobile.port.android.sync.UmAppDatabaseSyncWorker
import java.util.concurrent.TimeUnit


/**
 * This service schedules the first WorkManager job for the sync, and then tracks if the app is
 * in the foreground (or not).
 */
class UmAppDatabaseSyncService : Service() {

    private val mBinder = LocalServiceBinder()

    inner class LocalServiceBinder : Binder() {

        val service: UmAppDatabaseSyncService
            get() = this@UmAppDatabaseSyncService
    }

    val lifecycleObserver = object: DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            isInForeground = true
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            isInForeground = false
            lastForegroundTime = System.currentTimeMillis()
        }
    }

    override fun onCreate() {
        super.onCreate()
        isInForeground = true
        WorkManager.getInstance().cancelAllWorkByTag(UmAppDatabaseSyncWorker.TAG)

        UmAppDatabaseSyncWorker.queueSyncWorker(100, TimeUnit.MILLISECONDS);
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    override fun onDestroy() {
        super.onDestroy()

        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
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
