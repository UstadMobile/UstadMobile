package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.DiscussionPost.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = TABLE_ID , tracker = DiscussionPostReplicate::class)
@Triggers(arrayOf(
    Trigger(
        name = "discussionpost_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            """
                REPLACE INTO DiscussionPost(discussionPostUid, discussionPostReplyToPostUid,
                discussionPostTitle, discussionPostMessage, discussionPostStartDate, 
                discussionPostCourseBlockUid, discussionPostVisible, discussionPostArchive, 
                discussionPostStartedPersonUid, discussionPostClazzUid, discussionPostLct)
                
              
                VALUES(NEW.discussionPostUid, NEW.discussionPostReplyToPostUid,
                NEW.discussionPostTitle, NEW.discussionPostMessage, NEW.discussionPostStartDate, 
                NEW.discussionPostCourseBlockUid, NEW.discussionPostVisible, NEW.discussionPostArchive, 
                NEW.discussionPostStartedPersonUid, NEW.discussionPostClazzUid, NEW.discussionPostLct)
                
                
                /*psql ON CONFLICT (discussionPostUid) DO UPDATE 
                SET 
                discussionPostReplyToPostUid = EXCLUDED.discussionPostReplyToPostUid,
                discussionPostTitle = EXCLUDED.discussionPostTitle , 
                discussionPostMessage = EXCLUDED.discussionPostMessage , 
                discussionPostStartDate = EXCLUDED.discussionPostStartDate , 
                discussionPostCourseBlockUid = EXCLUDED.discussionPostCourseBlockUid, 
                discussionPostVisible = EXCLUDED.discussionPostVisible , 
                discussionPostArchive = EXCLUDED.discussionPostArchive , 
                discussionPostStartedPersonUid = EXCLUDED.discussionPostStartedPersonUid , 
                discussionPostClazzUid = EXCLUDED.discussionPostClazzUid, 
                discussionPostLct = EXCLUDED.discussionPostLct
                
                */
            """
        ]
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

    var discussionPostVisible: Boolean = true

    var discussionPostArchive: Boolean = false

    //The person who started this post
    var discussionPostStartedPersonUid: Long = 0

    // The Course Uid
    var discussionPostClazzUid: Long = 0

    @LastChangedTime
    @ReplicationVersionId
    var discussionPostLct: Long = 0

    companion object{
        const val TABLE_ID = 132
    }
}