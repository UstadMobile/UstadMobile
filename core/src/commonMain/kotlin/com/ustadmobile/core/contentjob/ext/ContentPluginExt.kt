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
import io.ktor.client.statement.*
import io.ktor.http.*
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


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
                contentJobItem.cjiItemProgress = 50 + (((bytesSentTotal / contentLength) * 100) / 2)
                progress.onProgress(contentJobItem)
            }
        }
    }
}