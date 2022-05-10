package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = Comments.TABLE_ID, tracker = CommentsReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "comments_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO Comments(commentsUid, commentsText, commentsEntityType, commentsEntityUid, commentsPublic, commentsStatus, commentsPersonUid, commentsToPersonUid, commentSubmitterUid, commentsFlagged, commentsInActive, commentsDateTimeAdded, commentsDateTimeUpdated, commentsMCSN, commentsLCSN, commentsLCB, commentsLct) 
         VALUES (NEW.commentsUid, NEW.commentsText, NEW.commentsEntityType, NEW.commentsEntityUid, NEW.commentsPublic, NEW.commentsStatus, NEW.commentsPersonUid, NEW.commentsToPersonUid, NEW.commentSubmitterUid, NEW.commentsFlagged, NEW.commentsInActive, NEW.commentsDateTimeAdded, NEW.commentsDateTimeUpdated, NEW.commentsMCSN, NEW.commentsLCSN, NEW.commentsLCB, NEW.commentsLct) 
         /*psql ON CONFLICT (commentsUid) DO UPDATE 
         SET commentsText = EXCLUDED.commentsText, commentsEntityType = EXCLUDED.commentsEntityType, commentsEntityUid = EXCLUDED.commentsEntityUid, commentsPublic = EXCLUDED.commentsPublic, commentsStatus = EXCLUDED.commentsStatus, commentsPersonUid = EXCLUDED.commentsPersonUid, commentsToPersonUid = EXCLUDED.commentsToPersonUid, commentSubmitterUid = EXCLUDED.commentSubmitterUid, commentsFlagged = EXCLUDED.commentsFlagged, commentsInActive = EXCLUDED.commentsInActive, commentsDateTimeAdded = EXCLUDED.commentsDateTimeAdded, commentsDateTimeUpdated = EXCLUDED.commentsDateTimeUpdated, commentsMCSN = EXCLUDED.commentsMCSN, commentsLCSN = EXCLUDED.commentsLCSN, commentsLCB = EXCLUDED.commentsLCB, commentsLct = EXCLUDED.commentsLct
         */"""
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

    @LastChangedTime
    @ReplicationVersionId
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
