package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity(
    indices = arrayOf(
        Index("commentsEntityUid", "commentsForSubmitterUid", name = "idx_comments_entity_submitter")
    )
)
@ReplicateEntity(
    tableId = Comments.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
 Trigger(
     name = "comments_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [TRIGGER_UPSERT],
 )
))
@Serializable
// only used for assignment comments
data class Comments(
    @PrimaryKey(autoGenerate = true)
    var commentsUid: Long = 0,

    var commentsText: String? = null,

    var commentsEntityUid : Long = 0,

    var commentsStatus: Int = COMMENTS_STATUS_APPROVED,

    // person uid of whoever made the comment
    var commentsFromPersonUid : Long = 0,

    /**
     * The submitter uid that these comments relate to.
     *
     * For course comments, this will be zero.
     * For private comments, it will be the submitter uid (e.g. personUid for individual assignments,
     * groupNum for group assignments).
     */
    var commentsForSubmitterUid: Long = 0,

    /**
     * The submitter UID (if any) of the person who submitted the comment
     */
    var commentsFromSubmitterUid: Long = 0,

    var commentsFlagged : Boolean = false,

    var commentsDeleted : Boolean = false,

    var commentsDateTimeAdded : Long = 0,

    @ReplicateLastModified
    @ReplicateEtag
    var commentsLct: Long = 0,
) {


    companion object {

        const val TABLE_ID = 208

        const val COMMENTS_STATUS_APPROVED = 0
    }


}
