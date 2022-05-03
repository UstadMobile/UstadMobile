package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = CourseAssignmentSubmission.TABLE_ID, tracker = CourseAssignmentSubmissionReplicate::class)
@Triggers(arrayOf(
        Trigger(
                name = "courseassignmentsubmission_remote_insert",
                order = Trigger.Order.INSTEAD_OF,
                on = Trigger.On.RECEIVEVIEW,
                events = [Trigger.Event.INSERT],
                sqlStatements = [
                    """REPLACE INTO CourseAssignmentSubmission(casUid, casAssignmentUid, casSubmitterUid, casSubmitterPersonUid, casText, casType, casTimestamp) 
         VALUES (NEW.casUid, NEW.casAssignmentUid, NEW.casSubmitterUid, NEW.casSubmitterPersonUid, NEW.casText, NEW.casType, NEW.casTimestamp) 
         /*psql ON CONFLICT (casUid) DO UPDATE 
         SET casAssignmentUid = EXCLUDED.casAssignmentUid, casSubmitterUid = EXCLUDED.casSubmitterUid, casSubmitterPersonUid = EXCLUDED.casSubmitterPersonUid, casText = EXCLUDED.casText, casType = EXCLUDED.casType, casTimestamp = EXCLUDED.casTimestamp
         */"""
                ]
        )
))
@Serializable
open class CourseAssignmentSubmission {

    @PrimaryKey(autoGenerate = true)
    var casUid: Long = 0

    var casAssignmentUid: Long = 0

    // if individual then personUid else groupNumber
    var casSubmitterUid: Long = 0

    var casSubmitterPersonUid: Long = 0

    var casText: String? = null

    var casType: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var casTimestamp: Long = 0

    companion object {

        const val TABLE_ID = 522

        const val SUBMISSION_TYPE_TEXT = 1
        const val SUBMISSION_TYPE_FILE = 2

        const val NOT_SUBMITTED = 0
        const val SUBMITTED = 1
        const val MARKED = 2

    }
}