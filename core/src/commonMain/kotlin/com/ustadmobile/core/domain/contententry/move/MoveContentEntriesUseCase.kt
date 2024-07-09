package com.ustadmobile.core.domain.contententry.move

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListSelectedItem
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentEntry

/**
 * Action moving ContentEntries by changing the parent on the ContentEntryParentChildJoin entity.
 *
 * Validate the move
 */
class MoveContentEntriesUseCase(
    private val repo: UmAppDatabase,
    private val systemImpl: UstadMobileSystemImpl,
) {

    suspend operator fun invoke(
        destContentEntry: ContentEntry,
        selectedEntriesToMove: Set<ContentEntryListSelectedItem>
    ) {
        if(selectedEntriesToMove.any {
                it.contentEntryUid == destContentEntry.contentEntryUid
        }) {
            throw IllegalArgumentException(systemImpl.getString(MR.strings.cannot_move_to_subfolder_of_self))
        }

        if(
            selectedEntriesToMove.any {
                it.parentContentEntryUid == destContentEntry.contentEntryUid
            }
        ) {
            throw IllegalArgumentException(systemImpl.getString(MR.strings.cannot_move_already_in_same_folder))
        }

        val uidsToMove = selectedEntriesToMove.map {
            it.contentEntryParentChildJoinUid
        }


        if(uidsToMove.isEmpty())
            return

        repo.contentEntryParentChildJoinDao().moveListOfEntriesToNewParent(
            contentEntryUid = destContentEntry.contentEntryUid,
            selectedItems = uidsToMove,
            updateTime = systemTimeInMillis()
        )
    }

}