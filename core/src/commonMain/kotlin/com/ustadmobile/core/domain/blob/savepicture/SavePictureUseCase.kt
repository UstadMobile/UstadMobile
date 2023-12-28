package com.ustadmobile.core.domain.blob.savepicture

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.util.systemTimeInMillis

class SavePictureUseCase(
    private val saveLocalUrisAsBlobUseCase: SaveLocalUrisAsBlobsUseCase,
    private val enqueueBlobUploadClientUseCase: EnqueueBlobUploadClientUseCase?,
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase,
) {

    suspend operator fun invoke(entityUid: Long, tableId: Int, pictureUri: String?) {
        if(pictureUri != null) {
            //HERE: can resize if desired
            val savedBlob = saveLocalUrisAsBlobUseCase(
                localUrisToSave = listOf(
                    SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                        localUri = pictureUri,
                        entityUid = entityUid,
                        tableId = tableId
                    )
                ),
            ).first()

            db.personPictureDao.updateUri(
                uid = entityUid,
                uri = savedBlob.blobUrl,
                time = systemTimeInMillis()
            )

            enqueueBlobUploadClientUseCase?.invoke(
                items = listOf(
                    EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem(
                    blobUrl = savedBlob.blobUrl,
                    tableId = tableId,
                    entityUid = entityUid,
                )),
                batchUuid = randomUuidAsString(),
            )
        }else {
            repo.personPictureDao.updateUri(
                uid = entityUid,
                uri = pictureUri,
                time = systemTimeInMillis(),
            )
        }
    }
}