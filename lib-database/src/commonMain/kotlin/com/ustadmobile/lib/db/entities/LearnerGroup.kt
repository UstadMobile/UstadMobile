package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = LearnerGroup.TABLE_ID, tracker = LearnerGroupReplicate::class)
@Serializable
@Triggers(arrayOf(
 Trigger(
     name = "learnergroup_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO LearnerGroup(learnerGroupUid, learnerGroupName, learnerGroupDescription, learnerGroupActive, learnerGroupMCSN, learnerGroupCSN, learnerGroupLCB, learnerGroupLct) 
         VALUES (NEW.learnerGroupUid, NEW.learnerGroupName, NEW.learnerGroupDescription, NEW.learnerGroupActive, NEW.learnerGroupMCSN, NEW.learnerGroupCSN, NEW.learnerGroupLCB, NEW.learnerGroupLct) 
         /*psql ON CONFLICT (learnerGroupUid) DO UPDATE 
         SET learnerGroupName = EXCLUDED.learnerGroupName, learnerGroupDescription = EXCLUDED.learnerGroupDescription, learnerGroupActive = EXCLUDED.learnerGroupActive, learnerGroupMCSN = EXCLUDED.learnerGroupMCSN, learnerGroupCSN = EXCLUDED.learnerGroupCSN, learnerGroupLCB = EXCLUDED.learnerGroupLCB, learnerGroupLct = EXCLUDED.learnerGroupLct
         */"""
     ]
 )
))
class LearnerGroup {

    @PrimaryKey(autoGenerate = true)
    var learnerGroupUid: Long = 0

    var learnerGroupName: String? = null

    var learnerGroupDescription: String? = null

    var learnerGroupActive: Boolean = true

    @MasterChangeSeqNum
    var learnerGroupMCSN: Long = 0

    @LocalChangeSeqNum
    var learnerGroupCSN: Long = 0

    @LastChangedBy
    var learnerGroupLCB: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var learnerGroupLct: Long = 0

    companion object {

        const val TABLE_ID = 301

    }
}