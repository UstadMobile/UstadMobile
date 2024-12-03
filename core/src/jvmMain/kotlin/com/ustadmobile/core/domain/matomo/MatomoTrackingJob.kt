package com.ustadmobile.core.domain.matomo

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.InterruptableCoroutineJob
import com.ustadmobile.core.domain.blob.upload.AbstractEnqueueBlobUploadClientUseCase
import com.ustadmobile.core.util.ext.di
import com.ustadmobile.core.util.ext.scheduleRetryOrThrow
import io.github.aakira.napier.Napier
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.JobExecutionContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class MatomoTrackingJob : InterruptableCoroutineJob() {

    override suspend fun executeAsync(context: JobExecutionContext) {
        val di = context.scheduler.di
        val jobDataMap = context.jobDetail.jobDataMap

        val screenName = jobDataMap.getString(RecordMatomoTrackingUseCaseJvmImpl.DATA_SCREEN_NAME)
        val path = jobDataMap.getString(RecordMatomoTrackingUseCaseJvmImpl.DATA_PATH)
        val endpoint = Endpoint(jobDataMap.getString(RecordMatomoTrackingUseCaseJvmImpl.DATA_ENDPOINT))

        if (endpoint.url.isBlank()) {
            Napier.e("MatomoTrackingCoroutineJob: Missing endpoint. Cannot track $screenName at $path.")
            return
        }
        val trackingUrl = "$endpoint/matomo.php?action_name=$screenName&url=$path&idsite=1&rec=1&apiv=1"


        // HTTP request to Matomo server
        val httpClient: HttpClient = di.direct.instance()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(trackingUrl))
            .GET()
            .build()

        try {
            val response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()

            if (response.statusCode() != 200) {
                throw Exception("Failed to track screen. Response code: ${response.statusCode()}, response body: ${response.body()}")
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

    companion object {
        const val MAX_ATTEMPTS_DEFAULT = 3
    }
}
