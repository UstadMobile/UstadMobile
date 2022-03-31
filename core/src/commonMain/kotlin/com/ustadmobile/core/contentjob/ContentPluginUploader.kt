package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.network.NetworkProgressListener
import com.ustadmobile.lib.db.entities.ContentJobItem
import io.ktor.client.*

/**
 * Interface (that can be mocked) used by ContentPlugins that may need to upload content to the
 * server.
 */
fun interface ContentPluginUploader {

    suspend fun upload(
            contentJobItem: ContentJobItem,
            progress: NetworkProgressListener?,
            httpClient: HttpClient,
            endpoint: Endpoint,
            processContext: ContentJobProcessContext,
    ): Int
}