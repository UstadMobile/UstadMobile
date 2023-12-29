package com.ustadmobile.lib.rest.api.blob

import com.ustadmobile.core.domain.blob.upload.BlobUploadRequest
import com.ustadmobile.core.domain.blob.upload.BlobUploadServerUseCase
import com.ustadmobile.lib.rest.domain.upload.ChunkedUploadRoute
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.BlobUploadServerRoute(
    useCase: (ApplicationCall) -> BlobUploadServerUseCase
) {
    post("upload-init-batch") {
        val request: BlobUploadRequest = call.receive()
        val response = useCase(call).onStartUploadSession(request)
        call.respond(response)
    }

    ChunkedUploadRoute(
        useCase = {
            useCase(it).batchChunkedUploadServerUseCase
        },
        path = "upload-batch-data",
    )

    ChunkedUploadRoute(
        useCase = {
            useCase(it).individualItemUploadServerUseCase
        },
        path = "upload-item",
    )

}