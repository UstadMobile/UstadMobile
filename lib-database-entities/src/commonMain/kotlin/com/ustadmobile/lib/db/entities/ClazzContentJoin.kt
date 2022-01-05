package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzContentJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = TABLE_ID, tracker = ClazzContentJoinReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "clazzcontentjoin_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO ClazzContentJoin(ccjUid, ccjContentEntryUid, ccjClazzUid, ccjActive, ccjLocalChangeSeqNum, ccjMasterChangeSeqNum, ccjLastChangedBy, ccjLct) 
         VALUES (NEW.ccjUid, NEW.ccjContentEntryUid, NEW.ccjClazzUid, NEW.ccjActive, NEW.ccjLocalChangeSeqNum, NEW.ccjMasterChangeSeqNum, NEW.ccjLastChangedBy, NEW.ccjLct) 
         /*psql ON CONFLICT (ccjUid) DO UPDATE 
         SET ccjContentEntryUid = EXCLUDED.ccjContentEntryUid, ccjClazzUid = EXCLUDED.ccjClazzUid, ccjActive = EXCLUDED.ccjActive, ccjLocalChangeSeqNum = EXCLUDED.ccjLocalChangeSeqNum, ccjMasterChangeSeqNum = EXCLUDED.ccjMasterChangeSeqNum, ccjLastChangedBy = EXCLUDED.ccjLastChangedBy, ccjLct = EXCLUDED.ccjLct
         */"""
     ]
 )
))
@Serializable
class ClazzContentJoin  {

    @PrimaryKey(autoGenerate = true)
    var ccjUid: Long = 0

    @ColumnInfo(index = true)
    var ccjContentEntryUid: Long = 0

    var ccjClazzUid: Long = 0

    var ccjActive: Boolean = true

    @LocalChangeSeqNum
    var ccjLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var ccjMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var ccjLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var ccjLct: Long = 0

    companion object {

        const val TABLE_ID = 134

    }

}