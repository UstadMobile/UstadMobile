package com.ustadmobile.core.domain.blob.savepicture

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.util.systemTimeInMillis

class SavePictureUseCase(
    private val saveLocalUrisAsBlobUseCase: SaveLocalUrisAsBlobsUseCase,
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase,
) {

    suspend operator fun invoke(entityUid: Long, tableId: Int, pictureUri: String?) {
        if(pictureUri != null) {
            saveLocalUrisAsBlobUseCase(
                localUrisToSave = listOf(
                    SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                        localUri = pictureUri,
                        uid = entityUid,
                        tableId = tableId
                    )
                ),
                onLocalUrisSavedToBlobUrls = object: SaveLocalUrisAsBlobsUseCase.OnLocalUrisSavedToBlobUrls {
                    override suspend fun invoke(savedBlobs: List<SaveLocalUrisAsBlobsUseCase.SavedBlob>) {
                        val savedBlob = savedBlobs.firstOrNull() ?: return

                        db.personPictureDao.updateUri(
                            uid = entityUid,
                            uri = savedBlob.localUri,
                            time = systemTimeInMillis()
                        )
                    }
                },

                onUploadProgress = object: SaveLocalUrisAsBlobsUseCase.OnLocalUriBlobUploadProgress {
                    override suspend fun onUploadProgressUpdate(
                        progressUpdates: List<SaveLocalUrisAsBlobsUseCase.BlobUploadProgress>
                    ) {

                    }

                    override suspend fun onComplete(
                        uploadResults: List<SaveLocalUrisAsBlobsUseCase.BlobUploadResult>
                    ) {
                        //Here: need to get the server node id
                        val activeRepoUid = (repo as DoorDatabaseRepository)
                    }
                }

            )
        }else {
            //should clear url by setting image as null
        }
    }
}