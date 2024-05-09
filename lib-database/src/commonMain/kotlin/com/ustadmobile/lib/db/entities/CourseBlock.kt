package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity(
    indices = arrayOf(
        Index("cbClazzUid", name = "idx_courseblock_cbclazzuid"),
        Index("cbSourcedId", name = "idx_courseblock_cbsourcedid"),
    )
)
@ReplicateEntity(
    tableId = CourseBlock.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
        Trigger(
                name = "courseblock_remote_insert",
                order = Trigger.Order.INSTEAD_OF,
                on = Trigger.On.RECEIVEVIEW,
                events = [Trigger.Event.INSERT],
                conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
                sqlStatements = [TRIGGER_UPSERT]
        )
))
@Serializable
data class CourseBlock(
    @PrimaryKey(autoGenerate = true)
    var cbUid: Long = 0,

    /**
     * If cbType is ContentEntry or Assignment
     * then cbEntityUid is the uid for the respective entity
     */
    var cbType: Int = 0,

    var cbIndentLevel: Int = 0,

    var cbModuleParentBlockUid: Long = 0,

    var cbTitle: String? = null,

    var cbDescription: String? = null,

    var cbCompletionCriteria: Int = 0,

    var cbHideUntilDate: Long = 0,

    var cbDeadlineDate: Long = Long.MAX_VALUE,

    var cbLateSubmissionPenalty: Int = 0,

    var cbGracePeriodDate: Long = Long.MAX_VALUE,

    var cbMaxPoints: Int = 10,

    var cbMinPoints: Int = 0,

    var cbIndex: Int = 0,

    @ColumnInfo(index = true)
    var cbClazzUid: Long = 0,

    var cbActive: Boolean = true,

    var cbHidden: Boolean = false,

    var cbEntityUid: Long = 0,

    @ReplicateLastModified
    @ReplicateEtag
    var cbLct: Long = 0,

    /**
     * The sourcedId as per the OneRoster API model e.g. as the CourseBlock is essentially a LineItem
     * as per the data model
     */
    var cbSourcedId: String? = null,

) {

    companion object {

        const val TABLE_ID = 124

        const val BLOCK_MODULE_TYPE = 100

        const val BLOCK_TEXT_TYPE = 102

        const val BLOCK_ASSIGNMENT_TYPE = 103

        const val BLOCK_CONTENT_TYPE = 104

        const val BLOCK_DISCUSSION_TYPE = 105

        /**
         * Represents a LineItme created using the OneRoster API by an external app.
         */
        const val BLOCK_EXTERNAL_APP = 300

    }

}