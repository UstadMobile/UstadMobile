package com.ustadmobile.core.domain.upload

/**
 * The response that will be provided for each request from the Chunked Upload endpoint.
 *
 * @param statusCode normally 204 (no content) or 200 (eg. when upload processing is finished and a
 *        body is returned)
 * @param body only string bodies are supported
 * @param contentType the content-type (if any) for the response
 * @param headers additional headers for the response (content-type and content-length will be set
 *        automatically).
 */
data class ChunkedUploadResponse(
    val statusCode: Int,
    val body: String?,
    val contentType: String?,
    val headers: Map<String, List<String>>
)
