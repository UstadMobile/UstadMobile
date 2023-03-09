package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = PeerReviewerAllocation.TABLE_ID, tracker = PeerReviewerAllocationReplicate::class)
@Triggers(arrayOf(
    Trigger(
        name = "peerreviewerallocation_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            """REPLACE INTO PeerReviewerAllocation(praUid, praMarkerSubmitterUid, praToMarkerSubmitterUid, praAssignmentUid, praActive, praLct) 
         VALUES (NEW.praUid, NEW.praMarkerSubmitterUid, NEW.praToMarkerSubmitterUid, NEW.praAssignmentUid, NEW.praActive, NEW.praLct) 
         /*psql ON CONFLICT (praUid) DO UPDATE 
         SET praMarkerSubmitterUid = EXCLUDED.praMarkerSubmitterUid, praToMarkerSubmitterUid = EXCLUDED.praToMarkerSubmitterUid, praAssignmentUid = EXCLUDED.praAssignmentUid, praActive = EXCLUDED.praActive, praLct = EXCLUDED.praLct
         */"""
        ]
    )
))
@Serializable
class PeerReviewerAllocation {

    @PrimaryKey(autoGenerate = true)
    var praUid: Long = 0

    // peer that is marking the assignment
    var praMarkerSubmitterUid: Long = 0

    // peer that is being marked
    var praToMarkerSubmitterUid: Long = 0

    var praAssignmentUid: Long = 0

    var praActive: Boolean = true

    @LastChangedTime
    @ReplicationVersionId
    var praLct: Long = 0

    companion object {

        const val TABLE_ID = 140

    }


}