package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
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
     sqlStatements = [
         TRIGGER_UPSERT_WHERE_NEWER
     ]
 )
))
@Serializable
// only used for assignment comments
open class Comments() {

    @PrimaryKey(autoGenerate = true)
    var commentsUid: Long = 0

    var commentsText: String? = null

    //Table name
    var commentsEntityType : Int = 0

    var commentsEntityUid : Long = 0

    var commentsPublic: Boolean = false

    var commentsStatus: Int = COMMENTS_STATUS_APPROVED

    // person uid of whoever made the comment
    var commentsPersonUid : Long = 0

    @Deprecated("use commentSubmitterUid")
    var commentsToPersonUid: Long = 0

    // personUid if individual, groupNum if group, 0 for class comment)
    var commentSubmitterUid: Long = 0

    var commentsFlagged : Boolean = false

    var commentsInActive : Boolean = false

    var commentsDateTimeAdded : Long = 0

    var commentsDateTimeUpdated: Long = 0

    @MasterChangeSeqNum
    var commentsMCSN: Long = 0

    @LocalChangeSeqNum
    var commentsLCSN: Long = 0

    @LastChangedBy
    var commentsLCB: Int = 0

    @ReplicateLastModified
    @ReplicateEtag
    var commentsLct: Long = 0

    constructor(table: Int, uid: Long, personUid: Long, now: Long, comment: String, isPublic: Boolean) : this() {
        commentsText = comment
        commentsEntityType = table
        commentsEntityUid = uid
        commentsPublic = isPublic
        commentsPersonUid = personUid
        commentsDateTimeAdded = now

    }

    companion object {

        const val TABLE_ID = 208

        const val COMMENTS_STATUS_APPROVED = 0
        const val COMMENTS_STATUS_PENDING = 1
        const val COMMENTS_STATUS_REJECTED = 2
        const val COMMENTS_STATUS_INAPPROPRIATE_REPORTED = 4
    }


}
