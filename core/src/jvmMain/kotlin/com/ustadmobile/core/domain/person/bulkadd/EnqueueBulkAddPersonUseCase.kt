package com.ustadmobile.core.domain.person.bulkadd

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import java.io.File

/**
 * Used on the HTTP server side to handle running bulk imports submitted from the web. The JS client
 * will submit the CSV file in a POST http request. The import itself will be run in a quartz job.
 * The status of each job will be held in BulkAddPersonStatusMap (a DI singleton). The JS client will
 * then make periodic requests to get the status until the job is done.
 */
class EnqueueBulkAddPersonUseCase(
    private val scheduler: Scheduler,
    private val learningSpace: LearningSpace,
    private val tmpDir: File,
) {

    suspend operator fun invoke(
        csvData: String,
    ): Long {
        val timestamp = systemTimeInMillis()
        val file = File(tmpDir, "$TMP_FILE_PREFIX$timestamp.csv")
        withContext(Dispatchers.IO) {
            file.parentFile?.takeIf { !it.exists() }?.mkdirs()
            file.writeText(csvData)
        }

        val jobDetail = JobBuilder.newJob(BulkAddPersonJob::class.java)
            .usingJobData(DATA_LEARNINGSPACE, learningSpace.url)
            .usingJobData(DATA_TIMESTAMP, timestamp)
            .build()

        val jobTrigger = TriggerBuilder.newTrigger()
            .withIdentity("$TRIGGER_KEY_PREFIX-$timestamp")
            .startNow()
            .build()

        scheduler.scheduleJob(jobDetail, jobTrigger)

        return timestamp
    }

    companion object {

        const val DATA_LEARNINGSPACE = "endpoint"

        const val DATA_TIMESTAMP = "csvTmpTimestamp"

        const val TMP_FILE_PREFIX = "bulk-add-person-"

        const val TRIGGER_KEY_PREFIX = "bulk-add-person-"

    }

}