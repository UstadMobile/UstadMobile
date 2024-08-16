package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

/**
 * CourseBlock - Can be:
 *  1) A Module (eg. use to collapse/expand other blocks)
 *  2) Text, Content, Assignment, or Discussion blocks
 *  3) Created by an external application via the OneRoster API
 *
 *  A CourseBlock is used as a LineItem in the OneRoster API. This allows OneRoster clients to access
 *  the course structure and assignment marks etc.
 *
 * @param cbUid The primary key - auto-generated if created internally within the app or an externally
 *         provided sourcedId is a valid long, otherwise the hash of cbSourcedId. See OneRosterEndpoint
 *         on mapping sourcedId to uid methodology.
 * @param cbMaxPoints The maximum points for this block, or null if this is not scored
 * @param cbMinPoints The minimum points for this block, or null if this is not scored
 */
@Entity(
    indices = arrayOf(
        Index("cbClazzUid", name = "idx_courseblock_cbclazzuid"),
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

    var cbMaxPoints: Float? = null,

    var cbMinPoints: Float? = null,

    var cbIndex: Int = 0,

    var cbClazzUid: Long = 0,

    var cbClazzSourcedId: String? = null,

    var cbActive: Boolean = true,

    var cbHidden: Boolean = false,

    var cbEntityUid: Long = 0,

    @ReplicateLastModified
    @ReplicateEtag
    var cbLct: Long = 0,

    var cbSourcedId: String? = null,

    var cbMetadata: String? = null,

    var cbCreatedByAppId: String? = null,
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