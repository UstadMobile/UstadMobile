package com.ustadmobile.core.domain.blob.savepicture

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ImageDao
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.cachestoragepath.getLocalUriIfRemote
import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.image.CompressImageUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentEntryPicture2
import com.ustadmobile.lib.db.entities.CourseBlockPicture
import com.ustadmobile.lib.db.entities.CoursePicture
import com.ustadmobile.lib.db.entities.PersonPicture
import io.github.aakira.napier.Napier

/**
 * Save a picture (compressed where required), updates the local database and repository as follows:
 * Android/JVM
 * 1) Attempt to compress the image and generate a thumbnail (using CompressImageUseCase)
 * 2) Store the compressed images in the local cache. Update the local database to the compressed
 *    images.
 * 3) Upload the image to server (if this isn't the server itself e.g. where enqueueBlobUploadClientCase != null)
 * 4) When upload is complete, because the entityUid and tableId is set on the generated TransferJobItem,
 *    an OutgoingReplication will be sent to the server to update the image path on the server.
 *
 * JS:
 * 1) Attempt to compress the image and generate a thumbnail (using CompressImageUseCase)
 * 2) Use saveLocalUriAsBlobUseCase - this will upload to the server and return a blob url.
 * 3) Directly update the image path for the entity on the repository.
 *
 * @param enqueueBlobUploadClientUseCase on platforms where a separate upload is required (e.g.
 *        Android and Desktop for non-local accounts). On the web, SaveLocalUriAsBlob does the uploads itself, so no
 *        upload client is required.
 */
class SavePictureUseCase(
    private val saveLocalUrisAsBlobUseCase: SaveLocalUrisAsBlobsUseCase,
    private val enqueueBlobUploadClientUseCase: EnqueueBlobUploadClientUseCase?,
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val compressImageUseCase: CompressImageUseCase,
    private val deleteUrisUseCase: DeleteUrisUseCase,
    private val getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase? = null,
) {

    private fun UmAppDatabase.imageDaoForTable(tableId: Int): ImageDao? {
        return when(tableId) {
            PersonPicture.TABLE_ID -> personPictureDao()
            CoursePicture.TABLE_ID -> coursePictureDao()
            CourseBlockPicture.TABLE_ID -> courseBlockPictureDao()
            ContentEntryPicture2.TABLE_ID -> contentEntryPicture2Dao()
            else -> null
        }
    }

    /**
     * @param entityUid the uid of the entity being used to store the picture
     * @param tableId the table id of the entity - PersonPicture, CoursePicture, CourseBlockPicture, ContentEntryPicture2
     * @param pictureUri the local uri of the picture that should be stored on the entity.
     *   On Android: the Android Uri (could be a file, uri returned from gallery, etc)
     *   On JS: Blob URL
     *   ON JVM: File URI
     */
    suspend operator fun invoke(
        entityUid: Long,
        tableId: Int,
        pictureUri: String?,
    ) {
        if(pictureUri?.ifEmpty { null } != null) {
            val pictureDoorUri = DoorUri.parse(pictureUri)
            val compressFromUri = getStoragePathForUrlUseCase
                ?.getLocalUriIfRemote(pictureDoorUri) ?: pictureDoorUri

            val mainCompressionResult = compressImageUseCase(
                fromUri = compressFromUri.toString()
            )

            val thumbnailCompressionResult = compressImageUseCase(
                fromUri = compressFromUri.toString(),
                params = CompressParams(
                    maxWidth = THUMBNAIL_DIMENSION,
                    maxHeight = THUMBNAIL_DIMENSION,
                )
            )

            val savedBlobs = saveLocalUrisAsBlobUseCase(
                localUrisToSave = buildList {
                    mainCompressionResult?.also {
                        add(
                            SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                                localUri = mainCompressionResult.uri,
                                entityUid = entityUid,
                                tableId = tableId,
                                mimeType = mainCompressionResult.mimeType,
                                deleteLocalUriAfterSave = true,
                                createRetentionLock = true,
                            )
                        )
                    }

                    thumbnailCompressionResult?.also {
                        add(
                            SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                                localUri = thumbnailCompressionResult.uri,
                                entityUid = entityUid,
                                tableId = tableId,
                                mimeType = thumbnailCompressionResult.mimeType,
                                deleteLocalUriAfterSave = true,
                                createRetentionLock = true,
                            )
                        )
                    }


                    /**
                     * The original picture uri should be saved as a blob when either compression
                     * result returns null (e.g. no further compression required) and the uri is
                     * local
                     */
                    val saveOriginalPictureUriAsBlob = (mainCompressionResult == null || thumbnailCompressionResult == null)
                            && !pictureDoorUri.isRemote()
                    if(saveOriginalPictureUriAsBlob) {
                        add(
                            SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                                localUri = pictureUri,
                                entityUid = entityUid,
                                tableId = tableId,
                                createRetentionLock = true,
                            )
                        )
                    }
                },
            )

            deleteUrisUseCase(listOf(pictureUri), onlyIfTemp = true)

            val originalPictureBlobUrl = savedBlobs.firstOrNull {
                it.localUri == pictureUri
            }?.blobUrl ?: pictureDoorUri.toString()

            val pictureBlobUrl = savedBlobs.firstOrNull {
                it.localUri == mainCompressionResult?.uri
            }?.blobUrl ?: originalPictureBlobUrl
            val thumbnailBlobUrl = savedBlobs.firstOrNull {
                it.localUri == thumbnailCompressionResult?.uri
            }?.blobUrl ?: originalPictureBlobUrl

            if(enqueueBlobUploadClientUseCase != null) {
                /*
                 * Where the original image uri is a remote URI, on Desktop/Android, it is probably
                 * only saved in the local cache at the moment and still needs to be uploaded to the
                 * server. E.g. the the content importer would have used SaveLocalUriAsBlob, however
                 * it has not been uploaded.
                 */
                val uploadOriginalUriItem = if(
                    (mainCompressionResult == null || thumbnailCompressionResult == null) && pictureDoorUri.isRemote()
                ) {
                    EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem(
                        blobUrl = pictureDoorUri.toString(),
                        tableId = tableId,
                        entityUid = entityUid,
                        retentionLockIdToRelease = 0,
                    )
                }else {
                    null
                }

                db.imageDaoForTable(tableId)?.updateUri(
                    uid = entityUid,
                    uri = pictureBlobUrl,
                    thumbnailUri = thumbnailBlobUrl,
                    time = systemTimeInMillis()
                )

                Napier.d("SavePictureUseCase: Set picture url = $pictureBlobUrl on entity=$entityUid table=$tableId")

                enqueueBlobUploadClientUseCase.invoke(
                    items = savedBlobs.map {
                        EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem(
                            blobUrl = it.blobUrl,
                            tableId = tableId,
                            entityUid = entityUid,
                            retentionLockIdToRelease = it.retentionLockId,
                        )
                    } + listOf(uploadOriginalUriItem).mapNotNull { it },
                    batchUuid = randomUuidAsString(),
                )
            }else {
                //No upload needed, directly update uri
                (repo ?: db).imageDaoForTable(tableId)?.updateUri(
                    uid = entityUid,
                    uri = pictureBlobUrl,
                    thumbnailUri = thumbnailBlobUrl,
                    time = systemTimeInMillis()
                )
            }
        }else {
            (repo ?: db).imageDaoForTable(tableId)?.updateUri(
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