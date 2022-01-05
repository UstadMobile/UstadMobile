package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = TABLE_ID, tracker = ContextXObjectStatementJoinReplicate::class)
@Serializable
@Triggers(arrayOf(
 Trigger(
     name = "contextxobjectstatementjoin_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO ContextXObjectStatementJoin(contextXObjectStatementJoinUid, contextActivityFlag, contextStatementUid, contextXObjectUid, verbMasterChangeSeqNum, verbLocalChangeSeqNum, verbLastChangedBy, contextXObjectLct) 
         VALUES (NEW.contextXObjectStatementJoinUid, NEW.contextActivityFlag, NEW.contextStatementUid, NEW.contextXObjectUid, NEW.verbMasterChangeSeqNum, NEW.verbLocalChangeSeqNum, NEW.verbLastChangedBy, NEW.contextXObjectLct) 
         /*psql ON CONFLICT (contextXObjectStatementJoinUid) DO UPDATE 
         SET contextActivityFlag = EXCLUDED.contextActivityFlag, contextStatementUid = EXCLUDED.contextStatementUid, contextXObjectUid = EXCLUDED.contextXObjectUid, verbMasterChangeSeqNum = EXCLUDED.verbMasterChangeSeqNum, verbLocalChangeSeqNum = EXCLUDED.verbLocalChangeSeqNum, verbLastChangedBy = EXCLUDED.verbLastChangedBy, contextXObjectLct = EXCLUDED.contextXObjectLct
         */"""
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

    @LastChangedTime
    @ReplicationVersionId
    var contextXObjectLct: Long = 0

    companion object {

        const val TABLE_ID = 66
    }
}
