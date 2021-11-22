package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.kodein.di.DI


expect suspend fun ContentPlugin.withWifiLock(context: Any, block: suspend () -> Unit)

expect suspend fun deleteFilesForContentEntry(contentEntryUid: Long, di: DI, endpoint: Endpoint): Int

@Deprecated("Replaced with ContentPluginUploader so it can be mocked in tests")
suspend fun ContentPlugin.uploadContentIfNeeded(contentNeedUpload: Boolean,
                                                contentJobItem: ContentJobItem,
                                                progress: ContentJobProgressListener,
                                                httpClient: HttpClient, torrentFileBytes: ByteArray,
                                                endpoint: Endpoint){
    if(contentNeedUpload) {
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

fun checkConnectivityToDoJob(db: UmAppDatabase, jobItemAndContentJob: ContentJobItemAndContentJob): Boolean{
    val connectivityState = db.connectivityStatusDao.status()?.connectivityState ?: throw IllegalStateException("no connectivity status")

    return !(connectivityState == ConnectivityStatus.STATE_DISCONNECTED ||
            (connectivityState == ConnectivityStatus.STATE_METERED && jobItemAndContentJob.contentJob?.cjIsMeteredAllowed == false))
}