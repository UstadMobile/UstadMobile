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
         """REPLACE INTO ClazzAssignment(caUid, caTitle, caDescription, caGroupUid, caActive, caClassCommentEnabled, caPrivateCommentsEnabled, caRequireFileSubmission, caFileType, caSizeLimit, caNumberOfFiles, caSubmissionPolicy, caMarkingType, caRequireTextSubmission, caTextLimitType, caTextLimit, caXObjectUid, caClazzUid, caLocalChangeSeqNum, caMasterChangeSeqNum, caLastChangedBy, caLct) 
         VALUES (NEW.caUid, NEW.caTitle, NEW.caDescription, NEW.caGroupUid, NEW.caActive, NEW.caClassCommentEnabled, NEW.caPrivateCommentsEnabled, NEW.caRequireFileSubmission, NEW.caFileType, NEW.caSizeLimit, NEW.caNumberOfFiles, NEW.caSubmissionPolicy, NEW.caMarkingType,NEW.caRequireTextSubmission, NEW.caTextLimitType, NEW.caTextLimit, NEW.caXObjectUid, NEW.caClazzUid, NEW.caLocalChangeSeqNum, NEW.caMasterChangeSeqNum, NEW.caLastChangedBy, NEW.caLct) 
         /*psql ON CONFLICT (caUid) DO UPDATE 
         SET caTitle = EXCLUDED.caTitle, caDescription = EXCLUDED.caDescription, caGroupUid = EXCLUDED.caGroupUid, caActive = EXCLUDED.caActive, caClassCommentEnabled = EXCLUDED.caClassCommentEnabled, caPrivateCommentsEnabled = EXCLUDED.caPrivateCommentsEnabled, caRequireFileSubmission = EXCLUDED.caRequireFileSubmission, caFileType = EXCLUDED.caFileType, caSizeLimit = EXCLUDED.caSizeLimit, caNumberOfFiles = EXCLUDED.caNumberOfFiles, caSubmissionPolicy = EXCLUDED.caSubmissionPolicy, caMarkingType = EXCLUDED.caMarkingType, caRequireTextSubmission = EXCLUDED.caRequireTextSubmission, caTextLimitType = EXCLUDED.caTextLimitType, caTextLimit = EXCLUDED.caTextLimit, caXObjectUid = EXCLUDED.caXObjectUid, caClazzUid = EXCLUDED.caClazzUid, caLocalChangeSeqNum = EXCLUDED.caLocalChangeSeqNum, caMasterChangeSeqNum = EXCLUDED.caMasterChangeSeqNum, caLastChangedBy = EXCLUDED.caLastChangedBy, caLct = EXCLUDED.caLct
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
    var caGroupUid: Long = 0

    var caActive: Boolean = true

    var caClassCommentEnabled: Boolean = true

    @ColumnInfo(defaultValue = "1")
    var caPrivateCommentsEnabled: Boolean = true

    @Deprecated("use on courseBlock, will be removed soon")
    @ColumnInfo(defaultValue = "100")
    var caCompletionCriteria: Int = COMPLETION_CRITERIA_SUBMIT

    @ColumnInfo(defaultValue = "1")
    var caRequireFileSubmission: Boolean = true

    @ColumnInfo(defaultValue = "0")
    var caFileType: Int = 0

    @ColumnInfo(defaultValue = "50")
    var caSizeLimit: Int = 50

    @ColumnInfo(defaultValue = "1")
    var caNumberOfFiles: Int = 1

    @ColumnInfo(defaultValue = "1")
    var caSubmissionPolicy: Int = SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE

    @ColumnInfo(defaultValue = "1")
    var caMarkingType: Int = MARKED_BY_COURSE_LEADER

    @ColumnInfo(defaultValue = "1")
    var caRequireTextSubmission: Boolean = true

    @ColumnInfo(defaultValue = "1")
    var caTextLimitType: Int = TEXT_WORD_LIMIT

    @ColumnInfo(defaultValue = "500")
    var caTextLimit: Int = 500

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

        const val SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE = 1
        const val SUBMISSION_POLICY_MULTIPLE_ALLOWED = 2

        const val MARKED_BY_COURSE_LEADER = 1
        const val MARKED_BY_PEERS = 2

        const val FILE_TYPE_ANY = 0
        const val FILE_TYPE_DOC = 1
        const val FILE_TYPE_IMAGE = 2
        const val FILE_TYPE_VIDEO = 3
        const val FILE_TYPE_AUDIO = 4

        const val FILE_SUBMISSION_NOT_REQUIRED = 0
        const val FILE_NOT_SUBMITTED = 1
        const val FILE_SUBMITTED = 2
        const val FILE_MARKED = 3

        const val TEXT_WORD_LIMIT = 1
        const val TEXT_CHAR_LIMIT = 2

        const val COMPLETION_CRITERIA_SUBMIT = 100
        const val COMPLETION_CRITERIA_GRADED = 102

    }


}