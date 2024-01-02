package com.ustadmobile.core.domain.upload

import kotlinx.io.files.Path

/**
 * @param request the final http request that was received (can be used to access headers etc)
 * @param uploadUuid the upload uuid
 * @param path the path to the completed upload file (this is a temporary file path).
 */
data class CompletedChunkedUpload(
    val request: ChunkedUploadRequest,
    val uploadUuid: String,
    val path: Path,
)

