package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Serializable
@Triggers(arrayOf(
 Trigger(
     name = "contextxobjectstatementjoin_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         TRIGGER_UPSERT_WHERE_NEWER
     ]
 )
))
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

    @ReplicateLastModified
    @ReplicateEtag
    var contextXObjectLct: Long = 0

    companion object {

        const val TABLE_ID = 66
    }
}
