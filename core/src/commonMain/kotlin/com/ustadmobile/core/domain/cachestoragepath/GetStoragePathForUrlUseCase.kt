package com.ustadmobile.core.domain.cachestoragepath

/**
 * There are various times where we want to get a (temporary) file path for a url, e.g.
 *
 * 1) When the user is importing video, pdf etc from a URL and we need to use tools that only
 *    work with files e.g. PDF import, FFProbe to check videos etc.
 *
 * 2) When we want to display a PDF file, the Android / Desktop PDF system needs a file, not a url.
 *
 * This use case will pull the content through the cache, then ask the cache for the storage path. It
 * can provide periodic state updates to allow the display of determinative progress when a file is
 * being downloaded for the user.
 */
interface GetStoragePathForUrlUseCase {

    data class GetStoragePathForUrlState(
        val fileUri: String? = null,
        val error: String? = null,
        val totalBytes: Long = 0,
        val bytesTransferred: Long = 0,
        val status: Status = Status.IN_PROGRESS,
    ) {

        enum class Status {
            IN_PROGRESS, COMPLETED, FAILED
        }

    }

    suspend operator fun invoke(
        url: String,
        progressInterval: Int = 500,
        onStateChange: (GetStoragePathForUrlState) -> Unit = { },
    ): String

}