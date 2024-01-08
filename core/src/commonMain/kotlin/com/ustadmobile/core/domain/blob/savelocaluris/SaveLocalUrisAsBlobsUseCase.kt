package com.ustadmobile.core.domain.blob.savelocaluris

import kotlinx.serialization.Serializable


/**
 * Save a list of local Uri(s) (e.g. Android Uri, JVM file URI, JS blob URI) as blobs.
 *
 * On Android/Desktop: Runs an SHA-256 checksum on the content and stores it into the local httpcache
 * under the blob url (e.g. https://endpoint.com/api/blob/sha256). The entry will be added with a
 * retention lock to prevent its eviction (e.g. it should never be evicted until upload is completed).
 *
 * On Javascript: uploads each item to the server using the blob upload-item API endpoint (see
 * Module.md)
 *
 */
interface SaveLocalUrisAsBlobsUseCase {

    /**
     * Item to be saved in the cache as a blob
     *
     * @param localUri the local uri on the system (as per DoorUri)
     * @param entityUid (optional) primary key for the related entity - will be passed on to become
     *        the TransferJobItem.tjiEntityUid . See its doc for details.
     * @param tableId (optional) table id for the related entitiy - will be passed on to become
     *        the TransferJobItem.tjiTableId. See its doc for details.
     * @param mimeType (optional), if not null, explicitly sets the content-type when it is stored
     *        as a blob. If not specified, the system UriHelper will be used to guess the mime type.
     */
    data class SaveLocalUriAsBlobItem(
        val localUri: String,
        val entityUid: Long = 0,
        val tableId: Int = 0,
        val mimeType: String? = null,
    )

    data class SavedBlob(
        val entityUid: Long,
        val localUri: String,
        val blobUrl: String,
        val retentionLockId: Int = 0,
    )

    /**
     * Represents a blob that was saved on the server side via the individual item upload endpoint.
     * Returned as the body on the last response
     */
    @Serializable
    data class ServerSavedBlob(
        val blobUrl: String
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