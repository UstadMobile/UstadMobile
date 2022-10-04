package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = CourseAssignmentMark.TABLE_ID, tracker = CourseAssignmentMarkReplicate::class)
@Triggers(arrayOf(
        Trigger(
                name = "courseassignmentmark_remote_insert",
                order = Trigger.Order.INSTEAD_OF,
                on = Trigger.On.RECEIVEVIEW,
                events = [Trigger.Event.INSERT],
                sqlStatements = [
                    """REPLACE INTO CourseAssignmentMark(camUid, camAssignmentUid, camSubmitterUid, camMark, camPenalty, camLct) 
         VALUES (NEW.camUid, NEW.camAssignmentUid, NEW.camSubmitterUid, NEW.camMark, NEW.camPenalty, NEW.camLct) 
         /*psql ON CONFLICT (camUid) DO UPDATE 
         SET camAssignmentUid = EXCLUDED.camAssignmentUid, camSubmitterUid = EXCLUDED.camSubmitterUid, camMark = EXCLUDED.camMark, camPenalty = EXCLUDED.camPenalty, camLct = EXCLUDED.camLct
         */"""
                ]
        )
))
@Serializable
class CourseAssignmentMark {

    @PrimaryKey(autoGenerate = true)
    var camUid: Long = 0

    var camAssignmentUid: Long = 0

    var camSubmitterUid: Long = 0

    var camMark: Float = 0f

    var camPenalty: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var camLct: Long = 0

    companion object {

        const val TABLE_ID = 523

    }
}