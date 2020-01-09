package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.port.sharedse.ext.generateConcatenatedFilesResponse
import io.ktor.application.call
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.close
import kotlinx.coroutines.io.writeFully
import kotlinx.io.core.IoBuffer
import java.io.ByteArrayInputStream
import java.io.File


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
        val entryFileListStr = call.parameters["entryFileList"]
        if(entryFileListStr == null) {
            call.respond(HttpStatusCode.BadRequest, "Entry file list not provided")
            return@get
        }

        val concatenatedResponse = db.containerEntryFileDao.generateConcatenatedFilesResponse(
                entryFileListStr)

        val inStream = concatenatedResponse.dataSrc
        if(inStream == null) {
            call.respond(HttpStatusCode.InternalServerError, "Internal Error: No input stream here")
            return@get
        }

        val headers = Headers.build {
            val eTagVal = concatenatedResponse.etag
            if(eTagVal != null) {
                etag(eTagVal)
            }
        }

        call.respond(object : OutgoingContent.WriteChannelContent() {

            override val contentType = ContentType.Application.OctetStream

            override val headers: Headers
                get() = headers

            override val contentLength: Long?
                get() = concatenatedResponse.contentLength

            override val status: HttpStatusCode = if(concatenatedResponse.status == 200) {
                HttpStatusCode.OK
            }else {
                HttpStatusCode(concatenatedResponse.status, "ConcatenatedResponseError")
            }

            override suspend fun writeTo(channel: ByteWriteChannel) {
                var bytesRead = 0
                val buf = ByteArray(8 * 1024)
                while(inStream.read(buf).also { bytesRead = it } != -1) {
                    channel.writeFully(buf, 0, bytesRead)
                }
                inStream.close()
                channel.flush()
                channel.close()
            }
        })

    }


}