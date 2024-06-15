package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzAssignment.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
 Trigger(
     name = "clazzassignment_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [TRIGGER_UPSERT],
 )
))

/**
 * Represents an Assignment for those with a student role in a given course.
 *
 * Assignments can be submitted by individuals (caGroupUid = 0) or groups (caGroupUid = the
 * CourseGroupSet uid).
 *
 * Submitter Uids will be the group number if submitting by group, or the personuid if submitting
 * individually. See CourseAssignmentSubmission.casSubmitterUid
 *
 * @param caGroupUid Assignments can be for individuals or groups. If the assignment should be submitted by each,
 * individual student, then caGroupUid = 0 (the default). If in groups, then caGroupUid is the
 * cgsUid of the CourseGroupSet
 * @param caPrivateCommentsEnabled True if submitters can submit a private comment (e.g. from student
 * to teacher, or peer to peer). Those with create learner record permission can always make a
 * private comment.
 *
 */
@Serializable
data class ClazzAssignment(
    @PrimaryKey(autoGenerate = true)
    var caUid: Long = 0,

    @Deprecated("Use title on courseblock")
    var caTitle: String? = null,

    @Deprecated("Use description on courseblock")
    var caDescription: String? = null,

    @ColumnInfo(defaultValue = "0")
    var caGroupUid: Long = 0,

    var caActive: Boolean = true,

    var caClassCommentEnabled: Boolean = true,

    @ColumnInfo(defaultValue = "1")
    var caPrivateCommentsEnabled: Boolean = true,

    @Deprecated("use on courseBlock, will be removed soon")
    @ColumnInfo(defaultValue = "100")
    var caCompletionCriteria: Int = COMPLETION_CRITERIA_SUBMIT,

    @ColumnInfo(defaultValue = "1")
    var caRequireFileSubmission: Boolean = true,

    @ColumnInfo(defaultValue = "0")
    var caFileType: Int = 0,

    @ColumnInfo(defaultValue = "50")
    var caSizeLimit: Int = 50,

    @ColumnInfo(defaultValue = "1")
    var caNumberOfFiles: Int = 1,

    @ColumnInfo(defaultValue = "$SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE")
    var caSubmissionPolicy: Int = SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE,

    @ColumnInfo(defaultValue = "$MARKED_BY_COURSE_LEADER")
    var caMarkingType: Int = MARKED_BY_COURSE_LEADER,

    @ColumnInfo(defaultValue = "1")
    var caRequireTextSubmission: Boolean = true,

    @ColumnInfo(defaultValue = "$TEXT_WORD_LIMIT")
    var caTextLimitType: Int = TEXT_WORD_LIMIT,

    @ColumnInfo(defaultValue = "500")
    var caTextLimit: Int = 500,

    @ColumnInfo(defaultValue = "0")
    var caXObjectUid: Long = 0,

    @ColumnInfo(index = true)
    var caClazzUid: Long = 0,

    @ColumnInfo(defaultValue = "0")
    var caPeerReviewerCount: Int = 0,

    @LocalChangeSeqNum
    var caLocalChangeSeqNum: Long = 0,

    @MasterChangeSeqNum
    var caMasterChangeSeqNum: Long = 0,

    @LastChangedBy
    var caLastChangedBy: Int = 0,

    @ReplicateLastModified
    @ReplicateEtag
    var caLct: Long = 0,
) {

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

        const val TEXT_WORD_LIMIT = 1
        const val TEXT_CHAR_LIMIT = 2

        const val COMPLETION_CRITERIA_SUBMIT = 100
        const val COMPLETION_CRITERIA_GRADED = 102

    }


}