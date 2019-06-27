package com.ustadmobile.port.android.sync

import android.content.Context
import androidx.work.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.port.android.netwokmanager.UmAppDatabaseSyncService
import java.util.concurrent.TimeUnit

class UmAppDatabaseSyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {


    override fun doWork(): Result {
        val activeAccount = UmAccountManager.getActiveAccount(applicationContext)

        val umAppDb = UmAppDatabase.getInstance(applicationContext)
        try {
           // umAppDb.syncWith(
            //        UmAccountManager.getRepositoryForActiveAccount(applicationContext),
          //          activeAccount?.personUid ?: 0, 100, 100)
            UMLog.l(UMLog.INFO, 100, "database syncWith repo ran")
        } catch (e: Exception) {
            UMLog.l(UMLog.WARN, 101, "Exception running syncWith :" + e.message)
        }

        if (!isStopped) {
            val appRecentlyActive = UmAppDatabaseSyncService.isInForeground || System.currentTimeMillis() - UmAppDatabaseSyncService.lastForegroundTime < UmAppDatabaseSyncService.SYNC_AFTER_BACKGROUND_LAG

            //TODO: Mike Update this to use new sync method
            /*
            if (appRecentlyActive || umAppDb.countPendingLocalChanges(UmAccountManager.getActivePersonUid(
                            applicationContext), umAppDb.getDeviceBits()) > 0) {
                queueSyncWorker((if (appRecentlyActive) 1 else 15).toLong(), TimeUnit.MINUTES)
            }*/
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
