package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
//@SyncableEntity(tableId = TABLE_ID)
@ReplicateEntity(tableId = TABLE_ID, tracker = ContextXObjectStatementJoinTracker::class)
@Serializable
//TODO: check this
class ContextXObjectStatementJoin {

    @PrimaryKey(autoGenerate = true)
    var contextXObjectStatementJoinUid: Long = 0

    var contextActivityFlag: Int = 0

    var contextStatementUid: Long = 0

    var contextXObjectUid: Long = 0

    @MasterChangeSeqNum
    var verbMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var verbLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var verbLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var contextXObjectLct: Long = 0

    companion object {

        const val TABLE_ID = 66
    }
}
