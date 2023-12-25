package com.ustadmobile.lib.rest.upload

import com.ustadmobile.core.domain.upload.ChunkedUploadRequest
import com.ustadmobile.core.domain.upload.ChunkedUploadResponse
import com.ustadmobile.core.domain.upload.ChunkedUploadServerUseCase
import com.ustadmobile.core.domain.upload.CompletedChunkedUpload
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
 * Upload Receiver that can accept a file upload in one or more chunks. Each upload request must
 * have a UUID in the header ( HEADER_UPLOAD_UUID ).
 *
 * The final chunk must include the header:
 * upload-final-chunk: true
 *
 * When receiving the final chunk, the onUploadCompleted callback will be called. The callback will
 * be responsible for sending a response.
 *
 * For other chunks, the data will be appended a 204 (no content) response will be sent.
 *
 * @param useCase ChunkedUploadServerUseCase to process requests
 * @param path the path on which this endpoint will use (e.g. server side url)
 * @param onUploadCompleted Completed upload handler
 */

fun Route.ChunkedUploadRoute(
    useCase: (ApplicationCall) -> ChunkedUploadServerUseCase,
    path: String,
    onUploadCompleted: suspend (CompletedChunkedUpload) -> ChunkedUploadResponse,
) {
    post(path) {
        val uploadUseCase = useCase(call)
        val chunkData = call.request.receiveChannel().toByteArray()

        val response = uploadUseCase.onChunkReceived(
            request = ChunkedUploadRequest(
                headers = call.request.headers.toMap(),
                chunkData = chunkData,
            ),
            onUploadComplete = onUploadCompleted,
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
