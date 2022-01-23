package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.network.NetworkProgressListener
import com.ustadmobile.lib.db.entities.ContentJobItem
import io.ktor.client.*

/**
 * Dummy uploader that does nothing. This is used on the KTOR server side where, in reality, local
 * files do not need uploaded
 */
class DummyContentPluginUploader(): ContentPluginUploader {
    override suspend fun upload(
        contentJobItem: ContentJobItem,
        progress: NetworkProgressListener?,
        httpClient: HttpClient,
        endpoint: Endpoint
    ): Int {
        return JobStatus.COMPLETE
    }
}