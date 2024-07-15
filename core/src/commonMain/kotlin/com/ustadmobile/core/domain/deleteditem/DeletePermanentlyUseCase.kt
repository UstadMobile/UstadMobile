package com.ustadmobile.core.domain.deleteditem

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.DeletedItem

class DeletePermanentlyUseCase(
    private val repoOrDb: UmAppDatabase
) {

    suspend operator fun invoke(
        items: List<DeletedItem>
    ) {
        val timeNow = systemTimeInMillis()

        repoOrDb.deletedItemDao().updateStatusByUids(
            uidList = items.map { it.delItemUid },
            newStatus = DeletedItem.STATUS_DELETED_PERMANENTLY,
            updateTime = timeNow
        )
    }

}