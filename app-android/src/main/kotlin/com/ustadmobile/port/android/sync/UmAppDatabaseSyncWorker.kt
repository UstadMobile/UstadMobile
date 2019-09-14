package com.ustadmobile.port.android.sync

import android.content.Context
import androidx.work.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.port.android.netwokmanager.UmAppDatabaseSyncService
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class UmAppDatabaseSyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {


    override fun doWork(): Result {
        try {
            println("Start sync")
            val clientRepo = UmAccountManager.getRepositoryForActiveAccount(applicationContext)
            runBlocking {
                val syncRepo =(clientRepo as DoorDatabaseSyncRepository)
                // TODO disabled until response is chunked up 
                //syncRepo.sync(null)
            }

            UMLog.l(UMLog.INFO, 100, "database syncWith repo ran")
        } catch (e: Exception) {
            UMLog.l(UMLog.WARN, 101, "Exception running syncWith :" + e.message)
        }

        if (!isStopped) {
            val appRecentlyActive = UmAppDatabaseSyncService.isInForeground || System.currentTimeMillis() - UmAppDatabaseSyncService.lastForegroundTime < UmAppDatabaseSyncService.SYNC_AFTER_BACKGROUND_LAG
            if (appRecentlyActive) {
                queueSyncWorker((if (appRecentlyActive) 1 else 15).toLong(), TimeUnit.MINUTES)
            }
        }


        return Result.success()
    }

    companion object {

        val TAG = "UmAppDbSync"

        fun queueSyncWorker(delay: Long, timeUnit: TimeUnit) {
            val workConstraint = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            val request = OneTimeWorkRequest.Builder(UmAppDatabaseSyncWorker::class.java)
                    .setInitialDelay(delay, timeUnit)
                    .addTag(TAG)
                    .setConstraints(workConstraint)
                    .build()
            WorkManager.getInstance().enqueue(request)
        }
    }
}
