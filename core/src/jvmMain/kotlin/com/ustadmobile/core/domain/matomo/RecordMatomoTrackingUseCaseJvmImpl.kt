package com.ustadmobile.core.domain.matomo

import com.ustadmobile.core.connectivitymonitor.ConnectivityTriggerGroupController
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey

class RecordMatomoTrackingUseCaseJvmImpl(
    private val scheduler: Scheduler,
    private val endpoint: String,
) : RecordMatomoTrackingUseCase {

    override suspend fun invoke(screenName: String, path: String) {
        val quartzJob = JobBuilder.newJob(MatomoTrackingJob::class.java)
            .usingJobData(DATA_SCREEN_NAME, screenName)
            .usingJobData(DATA_PATH, path)
            .usingJobData(DATA_ENDPOINT, endpoint)
            .build()

        val triggerKey = triggerKeyFor(screenName, path,endpoint)
        scheduler.unscheduleJob(triggerKey)

        val jobTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startNow()
            .build()

        scheduler.scheduleJob(quartzJob, jobTrigger)
    }

    companion object {
        const val DATA_SCREEN_NAME = "screenName"
        const val DATA_PATH = "path"
        const val DATA_ENDPOINT = "endpoint"

        fun triggerKeyFor(screenName: String, path: String, endpoint: String): TriggerKey {
            val endpointUrl = endpoint.take(100) // Truncate URL to 100 characters
            val screenNameHash = screenName.hashCode().toString()
            val pathHash = path.hashCode().toString()

            return TriggerKey(
                "matomo-track-${endpointUrl}-$screenNameHash-$pathHash",
                ConnectivityTriggerGroupController.TRIGGERKEY_CONNECTIVITY_REQUIRED_GROUP
            )
        }
    }
}
