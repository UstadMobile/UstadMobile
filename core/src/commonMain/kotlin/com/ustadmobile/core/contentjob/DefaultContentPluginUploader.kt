package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.db.entities.ContentJobItem
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

/**
 * Default implementation of uploading a container to the server
 */
class DefaultContentPluginUploader: ContentPluginUploader {

    //TODO: Needs to wait for the server to confirm completion of receiving the container
    override suspend fun upload(
        contentJobItem: ContentJobItem,
        progress: ContentJobProgressListener,
        httpClient: HttpClient,
        torrentFileBytes: ByteArray,
        endpoint: Endpoint
    ) {
        val containerUid = contentJobItem.cjiContainerUid
        val contentEntryUid = contentJobItem.cjiContentEntryUid
        val path = UMFileUtil.joinPaths(endpoint.url, "containers/${containerUid}/$contentEntryUid/upload")
        val torrentFileName = "${containerUid}.torrent"
        contentJobItem.cjiServerJobId = httpClient.post(path) {
            body = MultiPartFormDataContent(formData {
                append("torrentFile", torrentFileBytes,
                    Headers.build {
                        append(HttpHeaders.ContentType, "application/x-bittorrent")
                        append(HttpHeaders.ContentDisposition, "filename=$torrentFileName")
                    })
            })
            onUpload { bytesSentTotal, contentLength ->
                contentJobItem.cjiItemProgress = (contentJobItem.cjiItemTotal / 2) + (((bytesSentTotal / contentLength) * contentJobItem.cjiItemTotal) / 2)
                progress.onProgress(contentJobItem)
            }
        }
    }
}