package com.ustadmobile.core.domain.contententry.delete

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListSelectedItem
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis

class DeleteContentEntryParentChildJoinUseCase(
    private val repoOrDb: UmAppDatabase,
) {

    suspend operator fun invoke(
        entries: Set<ContentEntryListSelectedItem>,
        activeUserPersonUid: Long,
    ){
        val timeNow = systemTimeInMillis()
        repoOrDb.withDoorTransactionAsync {
            repoOrDb.contentEntryParentChildJoinDao().setEntriesDeleted(
                selectedUids = entries.map { it.contentEntryParentChildJoinUid },
                isDeleted = true,
                updateTime = timeNow,
            )

            entries.forEach {
                repoOrDb.deletedItemDao().insertDeletedItemForContentEntryParentChildJoin(
                    cepcjUid = it.contentEntryParentChildJoinUid,
                    time = timeNow,
                    deletedByPersonUid = activeUserPersonUid,
                )
            }
        }
    }

}