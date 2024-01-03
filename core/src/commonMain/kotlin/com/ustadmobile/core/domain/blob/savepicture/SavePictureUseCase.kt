package com.ustadmobile.core.domain.blob.savepicture

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ImageDao
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.CoursePicture
import com.ustadmobile.lib.db.entities.PersonPicture

/**
 * @param enqueueBlobUploadClientUseCase on platforms where a separate upload is required (e.g.
 *        Android and Desktop). On the web, SaveLocalUriAsBlob does the uploads itself, so no
 *        upload client is required.
 */
class SavePictureUseCase(
    private val saveLocalUrisAsBlobUseCase: SaveLocalUrisAsBlobsUseCase,
    private val enqueueBlobUploadClientUseCase: EnqueueBlobUploadClientUseCase?,
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase,
    private val compressImageUseCase: CompressUseCase,
) {

    private fun UmAppDatabase.imageDaoForTable(tableId: Int): ImageDao? {
        return when(tableId) {
            PersonPicture.TABLE_ID -> personPictureDao
            CoursePicture.TABLE_ID -> coursePictureDao
            else -> null
        }
    }

    suspend operator fun invoke(
        entityUid: Long,
        tableId: Int,
        pictureUri: String?,
    ) {
        if(pictureUri != null) {
            val mainCompressionResult = compressImageUseCase(fromUri = pictureUri)
            val thumbnailCompressionResult = compressImageUseCase(
                fromUri = pictureUri,
                params = CompressParams(
                    maxWidth = THUMBNAIL_DIMENSION,
                    maxHeight = THUMBNAIL_DIMENSION,
                )
            )

            val savedBlobs = saveLocalUrisAsBlobUseCase(
                localUrisToSave = listOf(
                    SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                        localUri = mainCompressionResult.uri,
                        entityUid = entityUid,
                        tableId = tableId,
                        mimeType = mainCompressionResult.mimeType,
                    ),
                    SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                        localUri = thumbnailCompressionResult.uri,
                        entityUid = entityUid,
                        tableId = tableId,
                        mimeType = mainCompressionResult.mimeType,
                    )
                ),
            )

            val pictureBlob = savedBlobs.first {
                it.localUri == mainCompressionResult.uri
            }
            val thumbnailBlob = savedBlobs.first {
                it.localUri == thumbnailCompressionResult.uri
            }

            if(enqueueBlobUploadClientUseCase != null) {
                db.imageDaoForTable(tableId)?.updateUri(
                    uid = entityUid,
                    uri = pictureBlob.blobUrl,
                    thumbnailUri = thumbnailBlob.blobUrl,
                    time = systemTimeInMillis()
                )

                enqueueBlobUploadClientUseCase.invoke(
                    items = savedBlobs.map {
                        EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem(
                            blobUrl = it.blobUrl,
                            tableId = tableId,
                            entityUid = entityUid,
                        )
                    },
                    batchUuid = randomUuidAsString(),
                )
            }else {
                //No upload needed, directly update repo
                repo.imageDaoForTable(tableId)?.updateUri(
                    uid = entityUid,
                    uri = pictureBlob.blobUrl,
                    thumbnailUri = thumbnailBlob.blobUrl,
                    time = systemTimeInMillis()
                )
            }
        }else {
            repo.imageDaoForTable(tableId)?.updateUri(
                uid = entityUid,
                uri = null,
                thumbnailUri = null,
                time = systemTimeInMillis(),
            )
        }
    }


    companion object {

        //List avatar size is 40 pixels as per
        // https://m3.material.io/components/lists/specs
        // Highest pixel density is 4x as per https://developer.android.com/training/multiscreen/screendensities
        const val THUMBNAIL_DIMENSION = 40 * 4

    }
}