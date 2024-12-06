package com.ustadmobile.core.domain.matomo

import com.ustadmobile.core.connectivitymonitor.ConnectivityTriggerGroupController
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey

/**
 * JVM-specific implementation of the `RecordMatomoTrackingUseCase` interface.
 *
 * This implementation utilizes the Quartz Scheduler library to schedule and execute
 * Matomo tracking jobs. It is designed for use in environments where asynchronous and
 * deferred tracking of analytics events is needed, such as desktop or server-side platforms.
 *
 * @param scheduler An instance of Quartz `Scheduler` used to schedule and manage tracking jobs.
 * @param endpoint The Matomo server endpoint URL where the tracking data will be sent.
 */
class RecordMatomoTrackingUseCaseJvmImpl(
    private val scheduler: Scheduler,
    private val endpoint: String,
) : RecordMatomoTrackingUseCase {

    /**
     * Tracks a Matomo event by scheduling a Quartz job for asynchronous execution.
     *
     * This method creates a `MatomoTrackingJob` with the provided screen name and path,
     * associates it with a unique trigger key, and schedules it for immediate execution.
     * If a job with the same trigger key already exists, it is unscheduled to avoid duplicates.
     *
     * @param screenName The title or identifier of the screen or feature being tracked.
     * @param path The URL or relative path of the screen being tracked.
     *
     */
    override suspend fun invoke(screenName: String, path: String) {
        val quartzJob = JobBuilder.newJob(MatomoTrackingJob::class.java)
            .usingJobData(DATA_SCREEN_NAME, screenName)
            .usingJobData(DATA_PATH, path)
            .usingJobData(DATA_ENDPOINT, endpoint)
            .build()

        val triggerKey = triggerKeyFor(screenName, path, endpoint)
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
