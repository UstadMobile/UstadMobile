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

    /**
     * Foreign key: assignment uid for the assignment, for which this is a submission
     */
    var casAssignmentUid: Long = 0

    /**
     * The submitterUid - the personUid of submitter if individual, the groupNum if this is by group.
     * A submitterUid of 0 means that the person is NOT a submitter for this assignment. To be
     * considered as a submitter:
     *   If the assignment requires individual submissions, the personUid must be enrolled in the
     *   course with the student role.
     *   If the assignment requires a group submission, the person must have a CourseGroupMember
     *   for the cgmGroupUid specified by the ClazzAssignment
     * A submitterUid of -1 means that the person is enrolled as a student in the course, however
     * it is a group assignment and the student is not assigned to any group.
     */
    var casSubmitterUid: Long = 0

    /**
     * The personUid for the submitter - whether this is a group assignment or individual - always the
     * personUid of the person who clicked submit
     */
    var casSubmitterPersonUid: Long = 0

    /**
     * The text of the assignment submission itself (HTML)
     */
    var casText: String? = null

    var casType: Int = 0

    /**
     * The timestamp for when this entry was submitted. Zero indicates that it is still a draft/not
     * saved in the database yet.
     */
    @LastChangedTime
    @ReplicationVersionId
    var casTimestamp: Long = 0

    companion object {

        const val TABLE_ID = 522

        const val SUBMITTER_ENROLLED_BUT_NOT_IN_GROUP = -1L

        const val SUBMISSION_TYPE_TEXT = 1
        const val SUBMISSION_TYPE_FILE = 2

        const val NOT_SUBMITTED = 0
        const val SUBMITTED = 1
        const val MARKED = 2

    }
}