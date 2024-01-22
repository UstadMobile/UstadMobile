package com.ustadmobile.core.domain.upload

/**
 * Domain UseCase that implements the server side of chunked uploads. This can be used by different
 * servers (e.g. KTOR, EmbeddedHTTPD, etc) as required.
 */
interface ChunkedUploadServerUseCase {

    /**
     * Function to call when a request is received. Each request should contain :
     *  Headers:
     *   upload-uuid to a UUID set by the client for the given upload
     *   upload-final-chunk to true on the final chunk uploaded
     *
     *  Body: should contain the binary chunk data
     *
     * @param request ChunkedUploadRequest representing the request received by the server
     * @return ChunkedUploadResponse representing the response to provide to the client including
     *         response code.
     */
    suspend fun onChunkReceived(
        request: ChunkedUploadRequest,
    ): ChunkedUploadResponse

}