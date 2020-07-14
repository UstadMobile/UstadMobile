package com.ustadmobile.lib.rest

import com.ustadmobile.lib.rest.ResumableUploadRoute.SESSIONID
import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receiveChannel
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.util.toByteArray
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.util.*


/**
 *  endpoint to create session
 *  endpoint to send data
 *  endpoint to return status
 *
 */
fun Route.ResumableUploadRoute(folder: File) {

    route("upload") {

        get("createSession") {
            val randomUuid = UUID.randomUUID()
            File(folder, randomUuid.toString())
            call.respondText(randomUuid.toString())
        }

        put("receiveData") {

            val contentLength = call.request.header(HttpHeaders.ContentLength)?.toLong() ?: 0L
            val rangeHeader = call.request.header(HttpHeaders.Range)
            val sessionId = call.request.header(SESSIONID) ?: ""

            val isValidRequest = isValidRequest(contentLength, rangeHeader)
            if (!isValidRequest) {
                call.respond(HttpStatusCode.BadRequest)
            } else {

                val input = call.receiveStream()
                val uploadFile = File(folder, sessionId)
                val fileOut = FileOutputStream(uploadFile, true)
                fileOut.write(IOUtils.toByteArray(input))

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}


fun isValidRequest(contentLength: Long, rangeHeader: String?): Boolean {

    if (rangeHeader.isNullOrBlank()) return false

    val header = rangeHeader.substring("bytes=".length)

    var fromByte = -1L
    var toByte = -1L

    try {
        val dashPos = header.indexOf('-')
        if (dashPos > 0) {
            fromByte = header.substring(0, dashPos).toLong()
        }

        if (dashPos == header.length - 1) {
            toByte = contentLength - 1
        } else if (dashPos > 0) {
            toByte = header.substring(dashPos + 1).toLong()
        }
        if (fromByte == -1L || toByte == -1L) {
            return false
        }
        /*
         * range request is inclusive: e.g. range 0-1 length is 2 bytes as per
         * https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html 14.35.1 Byte Ranges
         */
        val length = (toByte + 1) - fromByte
        return length == contentLength

    } catch (e: Exception) {
        return false
    }
}

object ResumableUploadRoute {

    const val SESSIONID = "sessionId"

}