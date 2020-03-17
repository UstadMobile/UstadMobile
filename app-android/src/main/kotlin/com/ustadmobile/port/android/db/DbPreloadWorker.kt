package com.ustadmobile.port.android.db

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import java.util.concurrent.TimeUnit

/**
 * Android implementation to call the database preload method.
 */
class DbPreloadWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val db = UmAccountManager.getActiveDatabase(applicationContext)
        db.preload()
        UstadMobileSystemImpl.instance.setAppPref(getPrefNameForActiveDb(applicationContext),
                true.toString(), applicationContext)
        return Result.success()
    }

    companion object {

        const val PREFKEY_PRELOADDONE_PREFIX = "preloaded-"

        fun getPrefNameForActiveDb(context: Context) = PREFKEY_PRELOADDONE_PREFIX + sanitizeDbNameFromUrl(UmAccountManager.getActiveEndpoint(context))

        fun scheduleWorkerIfNeeded(context: Context) {
            val prefkeyName = getPrefNameForActiveDb(context)
            val preloadDone = UstadMobileSystemImpl.instance.getAppPref(prefkeyName, context)?.toBoolean() ?: false
            if(!preloadDone) {
                WorkManager.getInstance(context).enqueue(
                        OneTimeWorkRequest.Builder(DbPreloadWorker::class.java)
                        .setInitialDelay(0, TimeUnit.MILLISECONDS)
                        .build())
            }
        }
    }
}