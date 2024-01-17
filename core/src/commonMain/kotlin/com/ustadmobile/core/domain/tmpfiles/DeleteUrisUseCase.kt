package com.ustadmobile.core.domain.tmpfiles

/**
 * UseCase wrapper for use in multiplatform use cases where uris need to be deleted. On Android/JVM
 * this normally means deleting a File in the temp directory (children of the directory DiTag.TAG_TMP_DIR).
 * On the web this means revoking the blob url.
 */
interface DeleteUrisUseCase {

    suspend operator fun invoke(
        uris: List<String>,
        onlyIfTemp: Boolean = true,
    )

}