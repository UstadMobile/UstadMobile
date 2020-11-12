package com.ustadmobile.port.android.sync

import android.content.Context
import androidx.work.*
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.port.android.netwokmanager.UmAppDatabaseSyncService
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.android.di
import org.kodein.di.instance
import org.kodein.di.on
import java.util.concurrent.TimeUnit

class UmAppDatabaseSyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val di: DI by di(applicationContext)

        //should not be needed anymore
//        EndpointScope.Default.activeEndpointUrls.forEach { endpointUrl ->
//            try {
//                val clientRepo: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = TAG_REPO)
//                runBlocking {
//                    (clientRepo as? DoorDatabaseSyncRepository)?.sync(null)
//                }
//            } catch (e: Exception) {
//                UMLog.l(UMLog.WARN, 101, "Exception running syncWith for $endpointUrl:" + e.message)
//            }
//        }


        if (!isStopped) {
            val appRecentlyActive = UmAppDatabaseSyncService.isInForeground ||
                    System.currentTimeMillis() - UmAppDatabaseSyncService.lastForegroundTime <
                    UmAppDatabaseSyncService.SYNC_AFTER_BACKGROUND_LAG
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

        /**
         * It will run the new OneTimeWorkRequests only if
         *             there is no pending work labelled with the TAG
         */
        fun queueSyncWorkerWithPolicy(delay: Long, timeUnit: TimeUnit,
                                      policy: ExistingWorkPolicy) {
            val workConstraint = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            val request = OneTimeWorkRequest.Builder(UmAppDatabaseSyncWorker::class.java)
                    .setInitialDelay(delay, timeUnit)
                    .addTag(TAG)
                    .setConstraints(workConstraint)
                    .build()
            WorkManager.getInstance().enqueueUniqueWork(TAG, policy, request)
        }
    }
}
