package com.ustadmobile.core.db

import android.content.Context
import androidx.work.*

class DbPreloadWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    //TODO: this needs to be made aware of multiple databases
    //val kodein by di()

    override fun doWork(): Result {
        //UmAccountManager.getActiveDatabase(applicationContext).runPreload()
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