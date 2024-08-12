package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.util.stringvalues.IStringValues
import com.ustadmobile.lib.db.entities.TransferJobItem
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
     * @param deleteLocalUriAfterSave if true, then the localUri given will be deleted after it has been
     *        successfully saved. On Android/JVM this means deleting the file. On JS this means
     *        revoking the blob URL.
     * @param createRetentionLock if true, then create a retention lock in the cache when storing if\
     *        running on a platform which using the local cache (e.g. lib-cache). On JS, this will be
     *        ignored.
     */
    data class SaveLocalUriAsBlobItem(
        val localUri: String,
        val entityUid: Long = 0,
        val tableId: Int = 0,
        val mimeType: String? = null,
        val deleteLocalUriAfterSave: Boolean = false,
        val createRetentionLock: Boolean = false,
        val extraHeaders: IStringValues = IStringValues.empty(),
    )

    /**
     * @param mimeType the mime type as it was saved. If it was specified in SaveLocalUriAsBlobItem,
     *        and this was the first time this checksum was saved, then it will have used the
     *        suggested mime type. Otherwise it will use the auto-guessed type / previous mime type
     * @param storageSize the size of the blob as it would be stored/transferred. If the blob is
     *        stored gzipped, then this is the size after gzip compression.
     */
    @Serializable
    data class SavedBlob(
        val entityUid: Long,
        val tableId: Int,
        val localUri: String,
        val blobUrl: String,
        val retentionLockId: Long = 0,
        val integrity: String,
        val mimeType: String,
        val storageSize: Long,
    )

    /**
     * Save a list of LocalUris as blobs.
     *
     * @param localUrisToSave event listener that will receive an event as local URIs are
     *        stored as blob URLs. This will be triggered when local storage is complete, so can be
     *        used to update the local database. If on Desktop/JVM, upload to the server is not yet
     *        done.
     * @param onTransferJobItemCreated On Javscript: this function will upload blobs to the server
     *        in order to get a URL. Anything that would wish to observe progress can use this
     *        TransferJobItem to observe progress.
     */
    suspend operator fun invoke(
        localUrisToSave: List<SaveLocalUriAsBlobItem>,
        onTransferJobItemCreated: (SaveLocalUriAsBlobItem, TransferJobItem) -> Unit = { _, _ -> },
    ): List<SavedBlob>

}