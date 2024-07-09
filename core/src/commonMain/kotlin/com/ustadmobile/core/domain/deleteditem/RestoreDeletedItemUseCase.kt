package com.ustadmobile.core.domain.deleteditem

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.DeletedItem

/**
 * Handle restoring a DeletedItem. This will run a DAO query/function based on the item type to
 * set its inactive/deleted flag to false, and then set the DeletedItem status to RESTORED.
 */
class RestoreDeletedItemUseCase(
    private val repoOrDb: UmAppDatabase
) {

    suspend operator fun invoke(items: List<DeletedItem>) {
        val itemsByType = items.groupBy { it.delItemEntityTable }
        val timeNow = systemTimeInMillis()

        repoOrDb.withDoorTransactionAsync {
            itemsByType.forEach { typeAndList ->
                when(typeAndList.key) {
                    ContentEntryParentChildJoin.TABLE_ID -> {
                        repoOrDb.contentEntryParentChildJoinDao().setEntriesDeleted(
                            selectedUids = typeAndList.value.map { it.delItemEntityUid },
                            isDeleted = false,
                            updateTime = timeNow
                        )
                    }
                }
            }

            repoOrDb.deletedItemDao().updateStatusByUids(
                uidList = items.map { it.delItemUid },
                newStatus = DeletedItem.STATUS_RESTORED,
                updateTime = timeNow
            )
        }
    }

}