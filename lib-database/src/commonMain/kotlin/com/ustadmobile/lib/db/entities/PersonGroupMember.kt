package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = PersonGroupMember.TABLE_ID, tracker = PersonGroupMemberReplicate::class,
    priority = ReplicateEntity.HIGHEST_PRIORITY)
@Triggers(arrayOf(
 Trigger(
     name = "persongroupmember_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO PersonGroupMember(groupMemberUid, groupMemberActive, groupMemberPersonUid, groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, groupMemberLastChangedBy, groupMemberLct) 
         VALUES (NEW.groupMemberUid, NEW.groupMemberActive, NEW.groupMemberPersonUid, NEW.groupMemberGroupUid, NEW.groupMemberMasterCsn, NEW.groupMemberLocalCsn, NEW.groupMemberLastChangedBy, NEW.groupMemberLct) 
         /*psql ON CONFLICT (groupMemberUid) DO UPDATE 
         SET groupMemberActive = EXCLUDED.groupMemberActive, groupMemberPersonUid = EXCLUDED.groupMemberPersonUid, groupMemberGroupUid = EXCLUDED.groupMemberGroupUid, groupMemberMasterCsn = EXCLUDED.groupMemberMasterCsn, groupMemberLocalCsn = EXCLUDED.groupMemberLocalCsn, groupMemberLastChangedBy = EXCLUDED.groupMemberLastChangedBy, groupMemberLct = EXCLUDED.groupMemberLct
         */"""
     ]
 )
))
class PersonGroupMember() {


    @PrimaryKey(autoGenerate = true)
    var groupMemberUid: Long = 0


    var groupMemberActive: Boolean = true

    @ColumnInfo(index = true)
    var groupMemberPersonUid: Long = 0

    @ColumnInfo(index = true)
    var groupMemberGroupUid: Long = 0

    @MasterChangeSeqNum
    var groupMemberMasterCsn: Long = 0

    @LocalChangeSeqNum
    var groupMemberLocalCsn: Long = 0

    @LastChangedBy
    var groupMemberLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var groupMemberLct: Long = 0

    constructor(personUid:Long, groupUid:Long) : this(){
        this.groupMemberPersonUid = personUid
        this.groupMemberGroupUid = groupUid
    }

    companion object {
        const val TABLE_ID = 44
    }
}
