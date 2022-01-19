package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = TABLE_ID, tracker = ClazzAssignmentContentJoinReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "clazzassignmentcontentjoin_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO ClazzAssignmentContentJoin(cacjUid, cacjContentUid, cacjAssignmentUid, cacjActive,cacjWeight, cacjMCSN, cacjLCSN, cacjLCB, cacjLct) 
         VALUES (NEW.cacjUid, NEW.cacjContentUid, NEW.cacjAssignmentUid, NEW.cacjActive, NEW.cacjWeight, NEW.cacjMCSN, NEW.cacjLCSN, NEW.cacjLCB, NEW.cacjLct) 
         /*psql ON CONFLICT (cacjUid) DO UPDATE 
         SET cacjContentUid = EXCLUDED.cacjContentUid, cacjAssignmentUid = EXCLUDED.cacjAssignmentUid, cacjActive = EXCLUDED.cacjActive, cacjWeight = EXCLUDED.cacjWeight, cacjMCSN = EXCLUDED.cacjMCSN, cacjLCSN = EXCLUDED.cacjLCSN, cacjLCB = EXCLUDED.cacjLCB, cacjLct = EXCLUDED.cacjLct
         */"""
     ]
 )
))
@Serializable
class ClazzAssignmentContentJoin {

    @PrimaryKey(autoGenerate = true)
    var cacjUid: Long = 0

    var cacjContentUid : Long = 0

    var cacjAssignmentUid : Long = 0

    var cacjActive : Boolean = true

    @ColumnInfo(defaultValue = "0")
    var cacjWeight: Int = 0

    @MasterChangeSeqNum
    var cacjMCSN: Long = 0

    @LocalChangeSeqNum
    var cacjLCSN: Long = 0

    @LastChangedBy
    var cacjLCB: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var cacjLct: Long = 0

    companion object {

        const val TABLE_ID = 521

    }

}