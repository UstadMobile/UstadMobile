package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.DiscussionPost.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = TABLE_ID ,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
    Trigger(
        name = "discussionpost_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = [TRIGGER_UPSERT],
    )
))
open class DiscussionPost() {

    @PrimaryKey(autoGenerate = true)
    var discussionPostUid: Long = 0

    /**
     * If this message is a top level post on the form, then discussionPostReplyToPostUid = 0.
     * Otherwise this is the discussionPostUid of the top level message.
     */
    var discussionPostReplyToPostUid: Long = 0

    var discussionPostTitle: String? = null

    //This is the HTML message
    var discussionPostMessage: String? = null

    var discussionPostStartDate: Long = 0

    /**
     * The CourseBlock uid of the discussion post
     */
    var discussionPostCourseBlockUid: Long = 0

    var dpDeleted: Boolean = false

    //The person who started this post
    var discussionPostStartedPersonUid: Long = 0

    // The Course Uid
    var discussionPostClazzUid: Long = 0

    @ReplicateLastModified
    @ReplicateEtag
    var discussionPostLct: Long = 0

    @Deprecated("No longer used - will be removed Aug/24")
    var discussionPostVisible: Boolean = false

    @Deprecated("No longer used - will be removed Aug/24")
    var discussionPostArchive: Boolean = false

    companion object{
        const val TABLE_ID = 132
    }
}