package com.ustadmobile.lib.rest

import com.ustadmobile.lib.rest.ResumableUploadRoute.SESSIONID
import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.ApplicationRequest
import io.ktor.request.header
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.put
import io.ktor.routing.route
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

            val sessionId = call.request.header(SESSIONID) ?: ""

            val (isValid, message) = call.request.isValidRequest(folder)
            if (!isValid) {
                call.respond(HttpStatusCode.BadRequest, message)
            } else {

                val input = call.receiveStream()
                val uploadFile = File(folder, sessionId)
                FileOutputStream(uploadFile, true).use { fileOut ->
                    fileOut.write(input.readBytes())
                    fileOut.flush()
                }

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

fun ApplicationRequest.isValidRequest(folder: File): Pair<Boolean, String> {

    val contentLength = header(HttpHeaders.ContentLength)?.toLong()
            ?: return Pair(false, "Content Length Missing")
    val rangeHeader = header(HttpHeaders.Range) ?: return Pair(false, "Range Missing")
    val sessionId = header(SESSIONID) ?: return Pair(false, "No Session Created")

    val uploadFile = File(folder, sessionId)
    val header = rangeHeader.substring("bytes=".length)

    var fromByte = -1L
    var toByte = -1L

    try {
        val dashPos = header.indexOf('-')
        if (dashPos > 0) {
            fromByte = header.substring(0, dashPos).toLong()
        }

        if (uploadFile.length() != fromByte) {
            return Pair(false, "Range should start from:${uploadFile.length()}")
        }

        if (dashPos == header.length - 1) {
            toByte = contentLength - 1
        } else if (dashPos > 0) {
            toByte = header.substring(dashPos + 1).toLong()
        }
        if (fromByte == -1L || toByte == -1L) {
            return Pair(false, "Range Given Invalid")
        }
        /*
         * range request is inclusive: e.g. range 0-1 length is 2 bytes as per
         * https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html 14.35.1 Byte Ranges
         */
        val length = (toByte + 1) - fromByte
        return if (length == contentLength) Pair(true, "") else Pair(false, "Range did not match Content-Length")

    } catch (e: Exception) {
        return Pair(false, "Unknown Error")
    }
}

object ResumableUploadRoute {

    const val SESSIONID = "sessionId"

}