package com.ustadmobile.core.domain.blob.saveandupload

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionFile

/**
 * UseCase that will save and uplaod the given local Uris. This works for 'attachments' e.g.
 * assignment submission files where the data will not be modified.
 *
 * On Android/JVM: Uris will be saved into the local cache first, then enqueued for upload.
 * On JS: SaveLocalUrisAsBlob itself will upload the items.
 */
class SaveAndUploadLocalUrisUseCase(
    private val saveLocalUrisAsBlobsUseCase: SaveLocalUrisAsBlobsUseCase,
    private val enqueueBlobUploadClientUseCase: EnqueueBlobUploadClientUseCase?,
    private val activeDb: UmAppDatabase,
    private val activeRepo: UmAppDatabase?,
) {


    private suspend fun UmAppDatabase.updateUris(
        blobs: List<SaveLocalUrisAsBlobsUseCase.SavedBlob>
    ) {
        val timeNow = systemTimeInMillis()
        withDoorTransactionAsync { _ ->
            blobs.forEach {
                when(it.tableId) {
                    CourseAssignmentSubmissionFile.TABLE_ID -> {
                        courseAssignmentSubmissionFileDao().updateUri(
                            casaUid = it.entityUid, uri = it.blobUrl, updateTime = timeNow
                        )
                    }
                }
            }
        }
    }

    suspend operator fun invoke(
        localUrisToSave: List<SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem>,
    ) {
        val blobs = saveLocalUrisAsBlobsUseCase(
            localUrisToSave
        )

        val enqueueBlobUploadClientUseCaseVal = enqueueBlobUploadClientUseCase
        if(enqueueBlobUploadClientUseCaseVal != null) {
            activeDb.updateUris(blobs)
            enqueueBlobUploadClientUseCaseVal(
                items = blobs.map {
                    EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem(
                        blobUrl = it.blobUrl,
                        entityUid = it.entityUid,
                        tableId = it.tableId,
                        retentionLockIdToRelease = it.retentionLockId,
                    )
                },
                batchUuid = randomUuidAsString()
            )
        }else {
            //There is no need to enqueue upload (JS and Server)
            (activeRepo ?: activeDb).updateUris(blobs)
        }

    }

}