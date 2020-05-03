package com.ustadmobile.staging.port.android.scheduler

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.staging.core.scheduler.ScheduledCheckRunner

class ScheduledCheckWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): ListenableWorker.Result {
        val db = UmAccountManager.getActiveDatabase(applicationContext)
        val repo = UmAccountManager.getRepositoryForActiveAccount(applicationContext)
        val check = db.scheduledCheckDao.findByUid(inputData.getLong(
                ARG_SCHEDULE_CHECK_UID, 0))
        if (check != null) {
            val checkRunner = ScheduledCheckRunner(check, db, repo)
            checkRunner.run()
            return ListenableWorker.Result.success()
        } else {
            return ListenableWorker.Result.failure()
        }
    }

    companion object {

        val ARG_SCHEDULE_CHECK_UID = "uid"
    }
}
