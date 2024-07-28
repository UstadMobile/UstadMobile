package com.ustadmobile.core.domain.contententry.save

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentEntryPicture2
import io.github.aakira.napier.Napier

class SaveContentEntryUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val enqueueSavePictureUseCase: EnqueueSavePictureUseCase,
) {

    /**
     * @param contentEntry The ContentEntry itself
     * @param joinToParentUid a content entry parent to join to, otherwise null
     * @param picture the ContentEntryPicture entity
     * @param initPictureUri the initial picture uri for this content entry.
     */
    suspend operator fun invoke(
        contentEntry: ContentEntry,
        joinToParentUid: Long?,
        picture: ContentEntryPicture2?,
        initPictureUri: String?,
    ) {
        val effectiveDb = (repo ?: db)
        effectiveDb.withDoorTransactionAsync {
            effectiveDb.contentEntryDao().upsertAsync(contentEntry)
            if(picture != null && picture.cepPictureUri != initPictureUri) {
                Napier.v {
                    "SavePictureUseCase: ContentEntry Set picture upsert uri = ${picture.cepPictureUri} uid=${picture.cepUid}"
                }
                db.contentEntryPicture2Dao().upsertListAsync(listOf(picture))
            }

            if(joinToParentUid != null) {
                effectiveDb.contentEntryParentChildJoinDao().insertAsync(
                    ContentEntryParentChildJoin(
                        cepcjParentContentEntryUid = joinToParentUid,
                        cepcjChildContentEntryUid = contentEntry.contentEntryUid,
                    )
                )
            }
        }

        //Run EnqueueSavePictureUseCase after the database transaction has finished.
        if(picture != null && picture.cepPictureUri != initPictureUri) {
            enqueueSavePictureUseCase(
                entityUid = picture.cepUid,
                tableId = ContentEntryPicture2.TABLE_ID,
                pictureUri = picture.cepPictureUri,
            )
        }
    }

}