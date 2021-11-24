package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.db.entities.ContentJobItem
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.*

/**
 * Default implementation of uploading a container to the server
 */
class DefaultContentPluginUploader: ContentPluginUploader {

    override suspend fun upload(
        contentJobItem: ContentJobItem,
        progress: ContentJobProgressListener,
        httpClient: HttpClient,
        torrentFileBytes: ByteArray,
        endpoint: Endpoint
    ) {
        withContext(Dispatchers.Default) {
            val containerUid = contentJobItem.cjiContainerUid
            val contentEntryUid = contentJobItem.cjiContentEntryUid
            val path = UMFileUtil.joinPaths(endpoint.url, "containers/${containerUid}/$contentEntryUid/upload")
            val torrentFileName = "${containerUid}.torrent"
            try {
                contentJobItem.cjiServerJobId = httpClient.post(path) {
                    body = MultiPartFormDataContent(formData {
                        append("torrentFile", torrentFileBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, "application/x-bittorrent")
                                    append(HttpHeaders.ContentDisposition, "filename=$torrentFileName")
                                })
                    })
                }
                val statusPath = UMFileUtil.joinPaths(endpoint.url, "containers/${contentJobItem.cjiServerJobId}/status")
                do{
                    delay(1000)
                    val serverJobItem: ContentJobItem = httpClient.get(statusPath)
                    contentJobItem.cjiItemProgress = (contentJobItem.cjiItemTotal / 2) +
                            (((serverJobItem.cjiRecursiveProgress / serverJobItem.cjiRecursiveTotal)
                                    * contentJobItem.cjiItemTotal) / 2)
                    progress.onProgress(contentJobItem)

                }while (serverJobItem.cjiRecursiveStatus <= JobStatus.COMPLETE_MIN)


            } catch (c: CancellationException) {
                httpClient.cancel()
            }
        }
    }
}