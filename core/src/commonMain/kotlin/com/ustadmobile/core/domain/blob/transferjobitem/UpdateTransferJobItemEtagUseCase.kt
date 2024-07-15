package com.ustadmobile.core.domain.blob.transferjobitem

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.PersonPicture

/**
 * Simple shared use case that will update the TransferJobItem.tjiEntityEtag as per the given
 * tableId and entity uid. This will ensure that when the TransferJobItem is queried to show the
 * user the status of an item, it can be linked to the current version (e.g. if the version of the
 * picture is updated, the transfer status of any previous version is no longer relevant).
 *
 * On Android/JVM, this is done as part of EnqueueBlobUploadClientUseCase. On Javascript, this is
 * done as part of SaveLocalUrisAsBlowUseCase.
 *
 * See TransferJobItem#tjiEntityEtag param docs for further notes.
 */
class UpdateTransferJobItemEtagUseCase {

    suspend operator fun invoke(
        db: UmAppDatabase,
        tableId: Int,
        entityUid: Long,
        transferJobItemUid: Int,
    ) {
        when(tableId) {
            PersonPicture.TABLE_ID -> {
                db.personPictureDao().updateTransferJobItemEtag(
                    entityUid = entityUid,
                    transferJobItemUid = transferJobItemUid,
                )
            }
            ContentEntryVersion.TABLE_ID -> {
                db.contentEntryVersionDao().updateTransferJobItemEtag(
                    entityUid = entityUid,
                    transferJobItemUid = transferJobItemUid,
                )
            }
            else -> {
                //do nothing
            }
        }
    }
}