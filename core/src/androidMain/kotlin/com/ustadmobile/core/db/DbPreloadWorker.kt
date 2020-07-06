package com.ustadmobile.core.db

import android.content.Context
import androidx.work.*
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.ext.runPreload

class DbPreloadWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    override fun doWork(): Result {
        UmAccountManager.getActiveDatabase(applicationContext).runPreload()
        return Result.success()
    }

    companion object {

        const val UNIQUE_WORK_NAME = "DbPreloadWorker"

        fun queuePreloadWorker(context: Context) {
            val request = OneTimeWorkRequest.Builder(DbPreloadWorker::class.java)
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.REPLACE, request)
        }
    }

}