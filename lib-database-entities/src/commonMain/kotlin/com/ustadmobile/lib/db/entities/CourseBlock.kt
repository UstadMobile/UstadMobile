package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = CourseBlock.TABLE_ID, tracker = CourseBlockReplicate::class)
@Triggers(arrayOf(
        Trigger(
                name = "courseblock_remote_insert",
                order = Trigger.Order.INSTEAD_OF,
                on = Trigger.On.RECEIVEVIEW,
                events = [Trigger.Event.INSERT],
                sqlStatements = [
                    """REPLACE INTO CourseBlock(cbUid, cbType, cbIndentLevel, cbModuleParentBlockUid, cbTitle, cbDescription, cbCompletionCriteria, cbHideUntilDate, cbDeadlineDate, cbLateSubmissionPenalty, cbGracePeriodDate, cbMaxPoints,cbMinPoints, cbIndex, cbClazzUid, cbActive,cbHidden, cbEntityUid, cbLct) 
         VALUES (NEW.cbUid, NEW.cbType, NEW.cbIndentLevel, NEW.cbModuleParentBlockUid, NEW.cbTitle, NEW.cbDescription, NEW.cbCompletionCriteria, NEW.cbHideUntilDate, NEW.cbDeadlineDate, NEW.cbLateSubmissionPenalty, NEW.cbGracePeriodDate, NEW.cbMaxPoints,NEW.cbMinPoints, NEW.cbIndex, NEW.cbClazzUid,NEW.cbActive, NEW.cbHidden, NEW.cbEntityUid, NEW.cbLct) 
         /*psql ON CONFLICT (cbUid) DO UPDATE 
         SET cbType = EXCLUDED.cbType, cbIndentLevel = EXCLUDED.cbIndentLevel, cbModuleParentBlockUid = EXCLUDED.cbModuleParentBlockUid, cbTitle = EXCLUDED.cbTitle, cbDescription = EXCLUDED.cbDescription, cbCompletionCriteria = EXCLUDED.cbCompletionCriteria, cbHideUntilDate = EXCLUDED.cbHideUntilDate,cbDeadlineDate = EXCLUDED.cbDeadlineDate, cbLateSubmissionPenalty = EXCLUDED.cbLateSubmissionPenalty, cbGracePeriodDate= EXCLUDED.cbGracePeriodDate, cbMaxPoints = EXCLUDED.cbMaxPoints, cbMinPoints = EXCLUDED.cbMinPoints, cbIndex = EXCLUDED.cbIndex,cbClazzUid = EXCLUDED.cbClazzUid, cbActive = EXCLUDED.cbActive, cbHidden = EXCLUDED.cbHidden, cbEntityUid = EXCLUDED.cbEntityUid, cbLct = EXCLUDED.cbLct
         */"""
                ]
        )
))
@Serializable
open class CourseBlock {

    @PrimaryKey(autoGenerate = true)
    var cbUid: Long = 0

    /**
     * If cbType is ContentEntry or Assignment
     * then cbEntityUid is the uid for the respective entity
     */
    var cbType: Int = 0

    var cbIndentLevel: Int = 0

    var cbModuleParentBlockUid: Long = 0

    var cbTitle: String? = null

    var cbDescription: String? = null

    var cbCompletionCriteria: Int = 0

    var cbHideUntilDate: Long = 0

    var cbDeadlineDate: Long = Long.MAX_VALUE

    var cbLateSubmissionPenalty: Int = 0

    var cbGracePeriodDate: Long = Long.MAX_VALUE

    var cbMaxPoints: Int = 10

    var cbMinPoints: Int = 0

    var cbIndex: Int = 0

    @ColumnInfo(index = true)
    var cbClazzUid: Long = 0

    var cbActive: Boolean = true

    var cbHidden: Boolean = false

    var cbEntityUid: Long = 0

    @LastChangedTime
    @ReplicationVersionId
    var cbLct: Long = 0

    companion object {

        const val TABLE_ID = 124

        const val BLOCK_MODULE_TYPE = 100

        const val BLOCK_TEXT_TYPE = 102

        const val BLOCK_ASSIGNMENT_TYPE = 103

        const val BLOCK_CONTENT_TYPE = 104

        const val BLOCK_DISCUSSION_TYPE = 105

    }
}