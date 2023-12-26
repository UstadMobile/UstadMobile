package com.ustadmobile.lib.rest.api.blob

import com.ustadmobile.core.domain.blob.upload.BlobUploadRequest
import com.ustadmobile.core.domain.blob.upload.BlobUploadServerUseCase
import com.ustadmobile.lib.rest.domain.upload.ChunkedUploadRoute
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.BlobUploadServerRoute(
    useCase: BlobUploadServerUseCase
) {
    post("upload-init") {
        val request: BlobUploadRequest = call.receive()
        val response = useCase.onStartUploadSession(request)
        call.respond(response)
    }

    ChunkedUploadRoute(
        useCase = {
            useCase.chunkedUploadServerUseCase
        },
        path = "upload"
    )

}