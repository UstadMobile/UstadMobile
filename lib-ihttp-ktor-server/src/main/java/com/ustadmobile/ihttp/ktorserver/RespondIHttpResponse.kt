package com.ustadmobile.ihttp.ktorserver

import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.response.IHttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondOutputStream
import io.ktor.server.response.respondText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.asSink

private val ktorReservedHeaders = listOf(
    "content-type", "transfer-encoding", "content-length"
)


/**
 * Send a response using a response from UstadCache. Test via TestContentEntryVersionRoute
 */
suspend fun ApplicationCall.respondIHttpResponse(
    iResponse: IHttpResponse?,
    iRequest: IHttpRequest? = null,
) {
    if(iResponse != null) {
        iResponse.headers.names().filter { headerName ->
            headerName.lowercase() !in ktorReservedHeaders
        }.forEach { headerName ->
            iResponse.headers.getAllByName(headerName).forEach { headerValue ->
                response.headers.append(headerName, headerValue)
            }
        }
        val contentLength = iResponse.headers["content-length"]?.toLong()
        val contentType = iResponse.headers["content-type"]

        val responseSource = iResponse.bodyAsSource()
        if(responseSource != null) {
            responseSource.use { source ->
                respondOutputStream(
                    contentType = contentType?.let { ContentType.parse(it) },
                    status = HttpStatusCode.fromValue(iResponse.responseCode),
                    contentLength = contentLength
                ) {
                    withContext(Dispatchers.IO) {
                        source.transferTo(this@respondOutputStream.asSink())
                        flush()
                    }
                }
            }
        }else {
            respondBytes(
                bytes = byteArrayOf(),
                contentType = contentType?.let { ContentType.parse(it) },
                status = HttpStatusCode.OK
            )
        }
    }else {
        respondText(
            text = "Not found: ${iRequest?.url ?: ""}",
            status = HttpStatusCode.NotFound
        )
    }
}
