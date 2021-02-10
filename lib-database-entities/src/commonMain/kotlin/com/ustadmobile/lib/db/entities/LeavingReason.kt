package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = LeavingReason.TABLE_ID)
@Serializable
class LeavingReason {

    @PrimaryKey(autoGenerate = true)
    var leavingReasonUid: Long = 0

    var leavingReasonTitle: String? = null

    @MasterChangeSeqNum
    var leavingReasonMCSN: Long = 0

    @LocalChangeSeqNum
    var leavingReasonCSN: Long = 0

    @LastChangedBy
    var leavingReasonLCB: Int = 0

    companion object {

        const val TABLE_ID = 410

    }
}