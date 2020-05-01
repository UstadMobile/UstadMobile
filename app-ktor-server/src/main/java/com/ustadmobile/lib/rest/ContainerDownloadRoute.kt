package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.lib.util.RANGE_CONTENT_ACCEPT_RANGE_HEADER
import com.ustadmobile.lib.util.RANGE_CONTENT_RANGE_HEADER
import com.ustadmobile.port.sharedse.ext.generateConcatenatedFilesResponse
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.request.httpMethod
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.head
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.toMap
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.close
import java.io.File


suspend fun PipelineContext<*, ApplicationCall>.serveConcatenatedResponse(db: UmAppDatabase) {
    val entryFileListStr = call.parameters["entryFileList"]
    if(entryFileListStr == null) {
        call.respond(HttpStatusCode.BadRequest, "Entry file list not provided")
        return
    }

    val concatenatedResponse = db.containerEntryFileDao.generateConcatenatedFilesResponse(
            entryFileListStr,
            method = call.request.httpMethod.value,
            requestHeaders = call.request.headers.toMap())

    val inStream = concatenatedResponse.dataSrc

    val headers = Headers.build {
        val eTagVal = concatenatedResponse.etag
        if(eTagVal != null) {
            etag(eTagVal)
        }

        set(RANGE_CONTENT_ACCEPT_RANGE_HEADER, "bytes")

        val rangeHeader = concatenatedResponse.responseHeaders[RANGE_CONTENT_RANGE_HEADER]
        if(rangeHeader != null)
            set(RANGE_CONTENT_RANGE_HEADER, rangeHeader)
    }

    call.respond(object : OutgoingContent.WriteChannelContent() {

        override val contentType = ContentType.Application.OctetStream

        override val headers: Headers
            get() = headers

        override val contentLength: Long?
            get() = concatenatedResponse.contentLength

        override val status: HttpStatusCode = HttpStatusCode.allStatusCodes
                .find { it.value == concatenatedResponse.status } ?: HttpStatusCode.InternalServerError

        override suspend fun writeTo(channel: ByteWriteChannel) {
            var bytesRead = 0
            if(inStream != null) {
                val buf = ByteArray(8 * 1024)
                while(inStream.read(buf).also { bytesRead = it } != -1) {
                    channel.writeFully(buf, 0, bytesRead)
                }
                inStream.close()
            }

            channel.flush()
            channel.close()
        }
    })
}

fun Route.ContainerDownload(db: UmAppDatabase) {
    route("ContainerEntryList") {
        get("findByContainerWithMd5") {
            val containerUid = call.request.queryParameters["containerUid"]?.toLong() ?: 0L
            val entryList = db.containerEntryDao.findByContainerWithMd5(containerUid)
            if(entryList.isNotEmpty()) {
                call.respond(entryList)
            }else {
                call.respond(HttpStatusCode.NotFound, "No such container $containerUid")
            }
        }
    }

    get("ContainerEntryFile/{entryFileUid}") {
        val entryFileUid = call.parameters["entryFileUid"]?.toLong() ?: 0L
        val entryFile = db.containerEntryFileDao.findByUid(entryFileUid)
        val filePath = entryFile?.cefPath
        if(filePath != null) {
            call.response.header("X-Content-Length-Uncompressed", entryFile?.ceTotalSize.toString())
            call.respondFile(File(filePath))
        }else {
            call.respond(HttpStatusCode.NotFound, "No such file: $entryFileUid")
        }
    }

    get("${ContainerEntryFileDao.ENDPOINT_CONCATENATEDFILES}/{entryFileList}") {
        serveConcatenatedResponse(db)
    }

    head("${ContainerEntryFileDao.ENDPOINT_CONCATENATEDFILES}/{entryFileList}") {
        serveConcatenatedResponse(db)
    }

}