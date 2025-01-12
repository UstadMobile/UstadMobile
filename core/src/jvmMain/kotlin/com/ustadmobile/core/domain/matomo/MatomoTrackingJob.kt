package com.ustadmobile.core.domain.matomo

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.blob.InterruptableCoroutineJob
import com.ustadmobile.core.util.ext.di
import com.ustadmobile.core.util.ext.scheduleRetryOrThrow
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.kodein.di.direct
import org.kodein.di.instance
import org.quartz.JobExecutionContext
import java.awt.Dimension
import java.awt.Toolkit
import java.util.Locale

/**
 * A Quartz job for asynchronously tracking Matomo analytics events.
 *
 * This job collects metadata about the user's environment (e.g., screen resolution, OS details) and sends
 * a tracking request to the Matomo server endpoint. It uses Ktor's `HttpClient` to make the network request
 * and retries the job in case of failure, leveraging Quartz's scheduling and retry mechanisms.
 *
 * The job uses `InterruptableCoroutineJob`, allowing it to run suspendable tasks within Quartz.
 */

class MatomoTrackingJob : InterruptableCoroutineJob() {

    /**
     * Executes the tracking job asynchronously.
     *
     * Collects relevant data (e.g., screen name, path, endpoint, resolution, language, OS info),
     * builds a request to the Matomo server, and submits the tracking data. If the request fails,
     * it schedules a retry with a default maximum retry limit.
     *
     * @param context The `JobExecutionContext` provided by Quartz, containing job-specific data and scheduler context.
     */

    override suspend fun executeAsync(context: JobExecutionContext) {
        val di = context.scheduler.di
        val jobDataMap = context.jobDetail.jobDataMap
        val screenName = jobDataMap.getString(RecordMatomoTrackingUseCaseJvmImpl.DATA_SCREEN_NAME)
        val path = jobDataMap.getString(RecordMatomoTrackingUseCaseJvmImpl.DATA_PATH)
        val endpoint =
            LearningSpace(jobDataMap.getString(RecordMatomoTrackingUseCaseJvmImpl.DATA_ENDPOINT))
        val resolution = getScreenResolution()
        val lang = Locale.getDefault().language
        val osName = System.getProperty("os.name") ?: "Unknown OS"
        val osVersion = System.getProperty("os.version") ?: "Unknown Version"
        val osArch = System.getProperty("os.arch") ?: "Unknown Architecture"

        if (endpoint.url.isBlank()) {
            Napier.e("MatomoTrackingCoroutineJob: Missing endpoint. Cannot track $screenName at $path.")
            return
        }

        try {
            val httpClient: HttpClient = di.direct.instance()
            val response: HttpResponse = httpClient.post("${endpoint.url}/matomo.php") {
                parameter(ACTION_NAME, screenName)
                parameter(URL_STRING, path)
                parameter(ID_SITE_STRING, ID_SITE)
                parameter(REC, REQ_REC)
                parameter(APIV, API_VERSION)
                parameter(RAND, (1..1_000_000).random().toString())
                parameter(ID, generateVisitorId())
                parameter(RES, resolution)
                parameter(LANG, lang)
                parameter(UA, "($osName $osVersion; $osArch)")
            }
            if (response.status.value != 200) {
                val body = response.body<String>()
                throw Exception("Failed to track screen. Response code: ${response.status.value}, response body: $body")
            }

            Napier.d("MatomoTrackingCoroutineJob: Successfully tracked $screenName at $path.")
        } catch (e: Throwable) {
            Napier.e("MatomoTrackingCoroutineJob: Failed to track $screenName at $path.", e)

            withContext(NonCancellable) {
                context.scheduleRetryOrThrow(
                    MatomoTrackingJob::class.java,
                    MAX_ATTEMPTS_DEFAULT
                )
            }
        }
    }

    /**
     * Generates a random visitor ID for tracking purposes.
     *
     * The ID is a 16-character string consisting of hexadecimal digits.
     *
     * @return A random visitor ID.
     */
    private fun generateVisitorId(): String {
        return (1..16)
            .map { ('a'..'f') + ('0'..'9') }
            .map { it.random() }
            .joinToString("")
    }

    /**
     * Retrieves the screen resolution of the device.
     *
     * The resolution is obtained using the Java `Toolkit` class, which provides the width and height
     * of the primary display in pixels.
     *
     * @return A string representation of the screen resolution in the format "widthxheight" (e.g., "1920x1080").
     */
    private fun getScreenResolution(): String {
        val toolkit: Toolkit = Toolkit.getDefaultToolkit()
        val screenSize: Dimension = toolkit.screenSize
        return "${screenSize.width}x${screenSize.height}"
    }

    companion object {
        const val ID_SITE = "1"
        const val REQ_REC = "1"
        const val API_VERSION = "1"
        const val MAX_ATTEMPTS_DEFAULT = 3
        const val ACTION_NAME = "action_name"
        const val URL_STRING = "url"
        const val ID_SITE_STRING = "idsite"
        const val REC = "rec"
        const val APIV = "apiv"
        const val RAND = "rand"
        const val ID = "_id"
        const val RES = "res"
        const val LANG = "lang"
        const val UA = "ua"
    }
}