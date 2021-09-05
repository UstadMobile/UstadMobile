package com.ustadmobile.core.contentjob

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.ForegroundUpdater
import androidx.work.WorkerParameters
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.android.di
import java.lang.IllegalStateException

class ContentJobRunnerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    val di: DI by closestDI(context)

    private fun ContentJobRunner.ContentJobResult.toWorkerResult(): Result {
        return when(this.status) {
            JobStatus.FAILED -> Result.failure()
            JobStatus.COMPLETE -> Result.success()
            else -> Result.retry()
        }
    }

    override suspend fun doWork(): Result {
        val endpointStr = inputData.getString(ContentJobManager.KEY_ENDPOINT)
            ?: throw IllegalStateException("No endpoint")
        val endpoint = Endpoint(endpointStr)

        val jobId = inputData.getLong(ContentJobManager.KEY_CONTENTJOB_UID, 0)

        val jobResult =  ContentJobRunner(jobId, endpoint, di).runJob().toWorkerResult()

        return jobResult
    }

}