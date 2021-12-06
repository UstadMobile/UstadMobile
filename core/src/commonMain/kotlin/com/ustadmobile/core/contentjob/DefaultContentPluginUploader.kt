package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.db.entities.ContentJobItem
import io.ktor.client.*
import kotlinx.coroutines.*

/**
 * Default implementation of uploading a container to the server
 */
class DefaultContentPluginUploader: ContentPluginUploader {

    override suspend fun upload(
        contentJobItem: ContentJobItem,
        progress: ContentJobProgressListener,
        httpClient: HttpClient,
        endpoint: Endpoint
    ) {
        withContext(Dispatchers.Default) {
            val containerUid = contentJobItem.cjiContainerUid
            val contentEntryUid = contentJobItem.cjiContentEntryUid

            try {

                // TODO upload



            } catch (c: CancellationException) {
                withContext(NonCancellable){
                    httpClient.cancel()
                    // TODO delete progress if cancelled
                }
                throw c
            }
        }
    }
}