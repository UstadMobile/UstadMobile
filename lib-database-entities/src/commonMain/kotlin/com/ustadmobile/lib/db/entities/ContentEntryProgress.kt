package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
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

    @LastChangedTime
    var contentEntryProgressLct: Long = 0

    companion object {
        const val CONTENT_ENTRY_PROGRESS_TABLE_ID = 210

        const val CONTENT_ENTRY_PROGRESS_FLAG_PASSED = 1
        const val CONTENT_ENTRY_PROGRESS_FLAG_FAILED = 2
        const val CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED = 4
        const val CONTENT_ENTRY_PROGRESS_FLAG_SATISFIED = 8
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContentEntryProgress) return false

        if (contentEntryProgressUid != other.contentEntryProgressUid) return false
        if (contentEntryProgressActive != other.contentEntryProgressActive) return false
        if (contentEntryProgressContentEntryUid != other.contentEntryProgressContentEntryUid) return false
        if (contentEntryProgressPersonUid != other.contentEntryProgressPersonUid) return false
        if (contentEntryProgressProgress != other.contentEntryProgressProgress) return false
        if (contentEntryProgressStatusFlag != other.contentEntryProgressStatusFlag) return false
        if (contentEntryProgressLocalChangeSeqNum != other.contentEntryProgressLocalChangeSeqNum) return false
        if (contentEntryProgressMasterChangeSeqNum != other.contentEntryProgressMasterChangeSeqNum) return false
        if (contentEntryProgressLastChangedBy != other.contentEntryProgressLastChangedBy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contentEntryProgressUid.hashCode()
        result = 31 * result + contentEntryProgressActive.hashCode()
        result = 31 * result + contentEntryProgressContentEntryUid.hashCode()
        result = 31 * result + contentEntryProgressPersonUid.hashCode()
        result = 31 * result + contentEntryProgressProgress
        result = 31 * result + contentEntryProgressStatusFlag
        result = 31 * result + contentEntryProgressLocalChangeSeqNum.hashCode()
        result = 31 * result + contentEntryProgressMasterChangeSeqNum.hashCode()
        result = 31 * result + contentEntryProgressLastChangedBy
        return result
    }

}

