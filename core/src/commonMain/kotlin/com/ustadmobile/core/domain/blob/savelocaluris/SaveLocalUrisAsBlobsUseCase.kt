package com.ustadmobile.core.domain.blob.savelocaluris


/**
 * Save a list of local Uri(s) (e.g. Android Uri, JVM file URI, JS blob URI) as blobs.
 *
 * On Android/Desktop: Runs an SHA-256 checksum on the content and stores it into the local httpcache
 * under the blob url (e.g. https://endpoint.com/api/blob/sha256).
 *
 */
interface SaveLocalUrisAsBlobsUseCase {

    data class SaveLocalUriAsBlobItem(
        val localUri: String,
        val entityUid: Long,
        //Optional: tableId can be set if desired
        val tableId: Int = 0,
    )

    data class SavedBlob(
        val entityUid: Long,
        val localUri: String,
        val blobUrl: String,
    )

    /**
     * Save a list of LocalUris as blobs.
     *
     * @param localUrisToSave event listener that will receive an event as local URIs are
     *        stored as blob URLs. This will be triggered when local storage is complete, so can be
     *        used to update the local database. If on Desktop/JVM, upload to the server is not yet
     *        done.
     */
    suspend operator fun invoke(
        localUrisToSave: List<SaveLocalUriAsBlobItem>,
    ): List<SavedBlob>

}