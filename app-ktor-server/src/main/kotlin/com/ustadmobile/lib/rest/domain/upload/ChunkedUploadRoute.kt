package com.ustadmobile.lib.rest.domain.upload

import com.ustadmobile.core.domain.upload.ChunkedUploadRequest
import com.ustadmobile.core.domain.upload.ChunkedUploadServerUseCase
import com.ustadmobile.core.util.ext.firstCaseInsensitiveOrNull
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.util.toByteArray
import io.ktor.util.toMap

/**
 * KTOR Route for ChunkedUploadServerUseCase
 *
 * @param useCase ChunkedUploadServerUseCase to process requests
 * @param path the path on which this endpoint will use (e.g. server side url, NOT the file storage path)
 */

fun Route.ChunkedUploadRoute(
    useCase: (ApplicationCall) -> ChunkedUploadServerUseCase,
    path: String,
) {
    post(path) {
        val uploadUseCase = useCase(call)
        val chunkData = call.request.receiveChannel().toByteArray()

        val response = uploadUseCase.onChunkReceived(
            request = ChunkedUploadRequest(
                headers = call.request.headers.toMap(),
                chunkData = chunkData,
            ),
        )

        call.respondText(
            contentType = response.headers.firstCaseInsensitiveOrNull("content-type")?.let {
                ContentType.parse(it)
            },
            text = response.body ?: "",
            status = HttpStatusCode.fromValue(response.statusCode),
        )
    }
}
