package com.ustadmobile.core.domain.matomo

import com.ustadmobile.core.account.Endpoint
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

class MatomoTrackingJob : InterruptableCoroutineJob() {

    override suspend fun executeAsync(context: JobExecutionContext) {
        val di = context.scheduler.di
        val jobDataMap = context.jobDetail.jobDataMap
        val screenName = jobDataMap.getString(RecordMatomoTrackingUseCaseJvmImpl.DATA_SCREEN_NAME)
        val path = jobDataMap.getString(RecordMatomoTrackingUseCaseJvmImpl.DATA_PATH)
        val endpoint =
            Endpoint(jobDataMap.getString(RecordMatomoTrackingUseCaseJvmImpl.DATA_ENDPOINT))
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

    private fun generateVisitorId(): String {
        return (1..16)
            .map { ('a'..'f') + ('0'..'9') }
            .map { it.random() }
            .joinToString("")
    }

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