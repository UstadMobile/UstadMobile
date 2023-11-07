package com.ustadmobile.core.domain.contententry.save

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin

class SaveContentEntryUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?
) {

    suspend operator fun invoke(
        contentEntry: ContentEntry,
        joinToParentUid: Long?,
    ) {
        val effectiveDb = (repo ?: db)
        effectiveDb.withDoorTransactionAsync {
            effectiveDb.contentEntryDao.upsertAsync(contentEntry)
            if(joinToParentUid != null) {
                effectiveDb.contentEntryParentChildJoinDao.insertAsync(
                    ContentEntryParentChildJoin(
                        cepcjParentContentEntryUid = joinToParentUid,
                        cepcjChildContentEntryUid = contentEntry.contentEntryUid,
                    )
                )
            }
        }
    }

}