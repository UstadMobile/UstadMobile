package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
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
                    """REPLACE INTO CourseAssignmentMark(camUid, camAssignmentUid, camSubmitterUid,camMarkerSubmitterUid, camMarkerPersonUid, camMarkerComment, camMark, camMaxMark, camPenalty, camLct) 
         VALUES (NEW.camUid, NEW.camAssignmentUid, NEW.camSubmitterUid, NEW.camMarkerSubmitterUid, NEW.camMarkerPersonUid, NEW.camMarkerComment, NEW.camMark, NEW.camMaxMark, NEW.camPenalty, NEW.camLct) 
         /*psql ON CONFLICT (camUid) DO UPDATE 
         SET camAssignmentUid = EXCLUDED.camAssignmentUid, camSubmitterUid = EXCLUDED.camSubmitterUid, camMarkerSubmitterUid = EXCLUDED.camMarkerSubmitterUid, camMarkerPersonUid = EXCLUDED.camMarkerPersonUid, camMarkerComment = EXCLUDED.camMarkerComment, camMark = EXCLUDED.camMark, camMaxMark = EXCLUDED.camMaxMark, camPenalty = EXCLUDED.camPenalty, camLct = EXCLUDED.camLct
         */"""
                ]
        )
))
@Serializable
open class CourseAssignmentMark {

    @PrimaryKey(autoGenerate = true)
    var camUid: Long = 0

    var camAssignmentUid: Long = 0

    var camSubmitterUid: Long = 0

    @ColumnInfo(defaultValue = "0")
    var camMarkerSubmitterUid: Long = 0

    @ColumnInfo(defaultValue = "0")
    var camMarkerPersonUid: Long = 0

    var camMarkerComment: String? = null

    /**
     * The mark issued to the submitter (e.g. group/student). This is the final mark, after
     * subtracting any late submission penalty
     */
    var camMark: Float = 0f

    /**
     * The maximum possible mark at the time the mark was given. Even if the CourseBlock is
     * changed later, this keeps a record
     */
    @ColumnInfo(defaultValue = "1")
    var camMaxMark: Float = 1f

    /**
     * The penalty for late submission, applied at the time that the mark is issued. Even if the
     * CourseBlock is changed later, this keeps a record.
     */
    var camPenalty: Float = 0f

    @LastChangedTime
    @ReplicationVersionId
    var camLct: Long = 0

    companion object {

        const val TABLE_ID = 523

    }
}