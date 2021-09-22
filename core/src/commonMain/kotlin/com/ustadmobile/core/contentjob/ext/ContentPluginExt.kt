package com.ustadmobile.core.contentjob.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.getLocalUri
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.kodein.di.instance
import org.kodein.di.on

suspend fun ContentPlugin.processMetadata(contentJob: ContentJobItemAndContentJob, process: ProcessContext, context: Any, endpoint: Endpoint): Long{
    val contentJobItem = contentJob.contentJobItem ?: throw IllegalArgumentException("missing job item")
    if(contentJobItem.cjiContentEntryUid == 0L){
        val uri = contentJobItem.sourceUri ?: throw IllegalStateException("missing uri")
        val doorUri = DoorUri.parse(uri)
        val localUri = process.getLocalUri(doorUri, context, di)
        val metadata = extractMetadata(localUri, process) ?: throw IllegalArgumentException("missing metadata")
        val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)
        contentJobItem.cjiContentEntryUid = repo.contentEntryDao.insert(metadata.entry)

        if(contentJobItem.cjiParentContentEntryUid == 0L){
            return contentJobItem.cjiContentEntryUid
        }

        ContentEntryParentChildJoin().apply {
            cepcjParentContentEntryUid = contentJobItem.cjiParentContentEntryUid
            cepcjChildContentEntryUid = contentJobItem.cjiContentEntryUid
            cepcjUid = repo.contentEntryParentChildJoinDao.insert(this)
        }

        return contentJobItem.cjiContentEntryUid
    }
    return contentJobItem.cjiContentEntryUid
}


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
                contentJobItem.cjiItemProgress = (bytesSentTotal / contentLength) * 100
                progress.onProgress(contentJobItem)
            }
        }
    }
}