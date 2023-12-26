package com.ustadmobile.core.domain.blob.savepicture

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.entities.OutgoingReplication
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.PersonPicture

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
                            uri = savedBlob.blobUrl,
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
                        val activeRepo = (repo as DoorDatabaseRepository)
                        val serverNodeId = activeRepo.remoteNodeIdOrFake()
                        val outgoingReplications = uploadResults.map {
                            OutgoingReplication(
                                destNodeId = serverNodeId,
                                orPk1 = it.uid,
                                orTableId = PersonPicture.TABLE_ID
                            )
                        }

                        db.outgoingReplicationDao.insert(outgoingReplications)
                    }
                }

            )
        }else {
            //should clear url by setting image as null
        }
    }
}