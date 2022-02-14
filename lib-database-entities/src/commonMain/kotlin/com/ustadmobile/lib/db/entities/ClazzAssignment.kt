package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzAssignment.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = TABLE_ID, tracker = ClazzAssignmentReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "clazzassignment_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO ClazzAssignment(caUid, caTitle, caDescription, caAssignmentType, caDeadlineDate, caStartDate, caLateSubmissionType, caLateSubmissionPenalty, caGracePeriodDate, caActive, caClassCommentEnabled, caPrivateCommentsEnabled, caRequireFileSubmission, caFileSubmissionWeight, caFileType, caSizeLimit, caNumberOfFiles, caEditAfterSubmissionType, caMarkingType, caMaxScore, caXObjectUid, caClazzUid, caLocalChangeSeqNum, caMasterChangeSeqNum, caLastChangedBy, caLct) 
         VALUES (NEW.caUid, NEW.caTitle, NEW.caDescription, NEW.caAssignmentType, NEW.caDeadlineDate, NEW.caStartDate, NEW.caLateSubmissionType, NEW.caLateSubmissionPenalty, NEW.caGracePeriodDate, NEW.caActive, NEW.caClassCommentEnabled, NEW.caPrivateCommentsEnabled, NEW.caRequireFileSubmission, NEW.caFileSubmissionWeight, NEW.caFileType, NEW.caSizeLimit, NEW.caNumberOfFiles, NEW.caEditAfterSubmissionType, NEW.caMarkingType, NEW.caMaxScore, NEW.caXObjectUid, NEW.caClazzUid, NEW.caLocalChangeSeqNum, NEW.caMasterChangeSeqNum, NEW.caLastChangedBy, NEW.caLct) 
         /*psql ON CONFLICT (caUid) DO UPDATE 
         SET caTitle = EXCLUDED.caTitle, caDescription = EXCLUDED.caDescription, caAssignmentType = EXCLUDED.caAssignmentType, caDeadlineDate = EXCLUDED.caDeadlineDate, caStartDate = EXCLUDED.caStartDate, caLateSubmissionType = EXCLUDED.caLateSubmissionType, caLateSubmissionPenalty = EXCLUDED.caLateSubmissionPenalty, caGracePeriodDate = EXCLUDED.caGracePeriodDate, caActive = EXCLUDED.caActive, caClassCommentEnabled = EXCLUDED.caClassCommentEnabled, caPrivateCommentsEnabled = EXCLUDED.caPrivateCommentsEnabled, caRequireFileSubmission = EXCLUDED.caRequireFileSubmission, caFileSubmissionWeight = EXCLUDED.caFileSubmissionWeight, caFileType = EXCLUDED.caFileType, caSizeLimit = EXCLUDED.caSizeLimit, caNumberOfFiles = EXCLUDED.caNumberOfFiles, caEditAfterSubmissionType = EXCLUDED.caEditAfterSubmissionType, caMarkingType = EXCLUDED.caMarkingType, caMaxScore = EXCLUDED.caMaxScore, caXObjectUid = EXCLUDED.caXObjectUid, caClazzUid = EXCLUDED.caClazzUid, caLocalChangeSeqNum = EXCLUDED.caLocalChangeSeqNum, caMasterChangeSeqNum = EXCLUDED.caMasterChangeSeqNum, caLastChangedBy = EXCLUDED.caLastChangedBy, caLct = EXCLUDED.caLct
         */"""
     ]
 )
))
@Serializable
open class ClazzAssignment {

    @PrimaryKey(autoGenerate = true)
    var caUid: Long = 0

    var caTitle: String? = null

    var caDescription: String? = null

    @ColumnInfo(defaultValue = "0")
    var caAssignmentType: Int = 0

    var caDeadlineDate: Long = Long.MAX_VALUE

    var caStartDate: Long = 0

    var caLateSubmissionType: Int = 0

    var caLateSubmissionPenalty: Int = 0

    var caGracePeriodDate: Long = Long.MAX_VALUE

    var caActive: Boolean = true

    var caClassCommentEnabled: Boolean = true

    var caPrivateCommentsEnabled: Boolean = false

    @ColumnInfo(defaultValue = "1")
    var caRequireFileSubmission: Boolean = true

    @ColumnInfo(defaultValue = "0")
    var caFileSubmissionWeight: Int = 0

    @ColumnInfo(defaultValue = "0")
    var caFileType: Int = 0

    @ColumnInfo(defaultValue = "50")
    var caSizeLimit: Int = 50

    @ColumnInfo(defaultValue = "1")
    var caNumberOfFiles: Int = 1

    @ColumnInfo(defaultValue = "0")
    var caEditAfterSubmissionType: Int = 0

    @ColumnInfo(defaultValue = "1")
    var caMarkingType: Int = MARKING_TYPE_TEACHER

    @ColumnInfo(defaultValue = "0")
    var caMaxScore: Int = 0

    @ColumnInfo(defaultValue = "0")
    var caXObjectUid: Long = 0

    @ColumnInfo(index = true)
    var caClazzUid: Long = 0

    @LocalChangeSeqNum
    var caLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var caMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var caLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var caLct: Long = 0

    companion object {

        const val TABLE_ID = 520

        const val ASSIGNMENT_LATE_SUBMISSION_REJECT = 1
        const val ASSIGNMENT_LATE_SUBMISSION_PENALTY = 2
        const val ASSIGNMENT_LATE_SUBMISSION_ACCEPT = 3

        const val ASSIGNMENT_TYPE_INDIVIDUAL = 1
        const val ASSIGNMENT_TYPE_GROUP = 2

        const val EDIT_AFTER_SUBMISSION_TYPE_ALLOWED_DEADLINE = 1
        const val EDIT_AFTER_SUBMISSION_TYPE_ALLOWED_GRACE = 2
        const val EDIT_AFTER_SUBMISSION_TYPE_NOT_ALLOWED = 3

        const val MARKING_TYPE_TEACHER = 1
        const val MARKING_TYPE_PEERS = 2

        const val FILE_TYPE_ANY = 0
        const val FILE_TYPE_DOC = 1
        const val FILE_TYPE_IMAGE = 2
        const val FILE_TYPE_VIDEO = 3
        const val FILE_TYPE_AUDIO = 4

        const val FILE_SUBMISSION_NOT_REQUIRED = 0
        const val FILE_NOT_SUBMITTED = 1
        const val FILE_SUBMITTED = 2
        const val FILE_MARKED = 3

    }


}