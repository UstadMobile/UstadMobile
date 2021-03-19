package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.io.ext.generateConcatenatedFilesResponse2
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.hexStringToByteArray
import com.ustadmobile.lib.util.RANGE_CONTENT_ACCEPT_RANGE_HEADER
import com.ustadmobile.lib.util.RANGE_CONTENT_RANGE_HEADER
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
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.jvm.javaio.toOutputStream
import org.kodein.di.direct
import org.kodein.di.instance
import java.io.File
import org.kodein.di.ktor.di
import org.kodein.di.on


fun Route.ContainerDownload() {

    suspend fun PipelineContext<*, ApplicationCall>.serveConcatenatedResponse2() {
        val entryMd5s = call.parameters["entryFileMd5s"]

        if(entryMd5s == null) {
            call.respond(HttpStatusCode.BadRequest, "entryFileMd5s parameter missing")
            return
        }

        val entryMd5List = entryMd5s.split(";").map {
            it.hexStringToByteArray().encodeBase64()
        }

        val db : UmAppDatabase = di().direct.on(call).instance(tag = DoorTag.TAG_DB)

        val concatenatedResponse = db.containerEntryFileDao.generateConcatenatedFilesResponse2(
                entryMd5List, call.request.headers.toMap(), db)

        val headers = Headers.build {
            set(RANGE_CONTENT_ACCEPT_RANGE_HEADER, "bytes")

            concatenatedResponse.rangeResponse?.responseHeaders?.get(RANGE_CONTENT_RANGE_HEADER)?.also {
                set(RANGE_CONTENT_RANGE_HEADER, it)
            }
        }

        call.respond(object: OutgoingContent.WriteChannelContent() {
            override val contentType = ContentType.Application.OctetStream

            override val headers: Headers
                get() = headers

            override val contentLength: Long
                get() = concatenatedResponse.actualContentLength

            override val status: HttpStatusCode = HttpStatusCode.allStatusCodes
                    .find { it.value == concatenatedResponse.status } ?: HttpStatusCode.InternalServerError

            override suspend fun writeTo(channel: ByteWriteChannel) {
                channel.toOutputStream().also {
                    if(call.request.httpMethod != HttpMethod.Head) {
                        concatenatedResponse.writeTo(it)
                    }

                    it.close()
                }
            }
        })
    }

    get("${ContainerEntryFileDao.ENDPOINT_CONCATENATEDFILES2}/{entryFileMd5s}") {
        serveConcatenatedResponse2()
    }

    head("${ContainerEntryFileDao.ENDPOINT_CONCATENATEDFILES2}/{entryFileMd5s}") {
        serveConcatenatedResponse2()
    }

    route("ContainerEntryList") {
        get("findByContainerWithMd5") {
            val db: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_DB)
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
        val db: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_DB)
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

}