package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.ContentEntryProgress.Companion.CONTENT_ENTRY_PROGRESS_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = CONTENT_ENTRY_PROGRESS_TABLE_ID)
@Serializable
open class ContentEntryProgress {

    @PrimaryKey(autoGenerate = true)
    var contentEntryProgressUid: Long = 0

    var contentEntryProgressActive: Boolean = true

    var contentEntryProgressContentEntryUid: Long = 0L

    var contentEntryProgressPersonUid : Long = 0L

    var contentEntryProgressProgress: Int = 0

    var contentEntryProgressStatusFlag : Int = 0

    @LocalChangeSeqNum
    var contentEntryProgressLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var contentEntryProgressMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var contentEntryProgressLastChangedBy: Int = 0

    companion object {
        const val CONTENT_ENTRY_PROGRESS_TABLE_ID = 210

        const val CONTENT_ENTRY_PROGRESS_FLAG_PASSED = 1
        const val CONTENT_ENTRY_PROGRESS_FLAG_FAILED = 2
        const val CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED = 4
        const val CONTENT_ENTRY_PROGRESS_FLAG_SATISFIED = 8
    }

}

