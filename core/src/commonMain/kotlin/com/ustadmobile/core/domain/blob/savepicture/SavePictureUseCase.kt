package com.ustadmobile.core.domain.blob.savepicture

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.util.systemTimeInMillis

class SavePictureUseCase(
    private val saveLocalUrisAsBlobUseCase: SaveLocalUrisAsBlobsUseCase,
    private val enqueueBlobUploadClientUseCase: EnqueueBlobUploadClientUseCase?,
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase,
    private val compressImageUseCase: CompressUseCase? = null,
) {

    suspend operator fun invoke(
        entityUid: Long,
        tableId: Int,
        pictureUri: String?,
        mimeType: String? = null,
    ) {
        if(pictureUri != null) {
            val compressionResult = compressImageUseCase?.invoke(fromUri = pictureUri)
            val uriToStore = compressionResult?.uri ?: pictureUri
            val mimeTypeToStore = compressionResult?.mimeType ?: mimeType

            val savedBlob = saveLocalUrisAsBlobUseCase(
                localUrisToSave = listOf(
                    SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                        localUri = uriToStore,
                        entityUid = entityUid,
                        tableId = tableId,
                        mimeType = mimeTypeToStore,
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