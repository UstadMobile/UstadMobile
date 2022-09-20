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
                REPLACE INTO DiscussionPost(discussionPostUid, 
                discussionPostTitle, discussionPostMessage, discussionPostStartDate, 
                discussionPostDiscussionTopicUid, discussionPostVisible, discussionPostArchive, 
                discussionPostStartedPersonUid, discussionPostClazzUid, discussionPostLct)
                
              
                VALUES(NEW.discussionPostUid, 
                NEW.discussionPostTitle, NEW.discussionPostMessage, NEW.discussionPostStartDate, 
                NEW.discussionPostDiscussionTopicUid, NEW.discussionPostVisible, NEW.discussionPostArchive, 
                NEW.discussionPostStartedPersonUid, NEW.discussionPostClazzUid, NEW.discussionPostLct)
                
                
                /*psql ON CONFLICT (discussionPostUid) DO UPDATE 
                SET discussionPostTitle = EXCLUDED.discussionPostTitle , 
                discussionPostMessage = EXCLUDED.discussionPostMessage , 
                discussionPostStartDate = EXCLUDED.discussionPostStartDate , 
                discussionPostDiscussionTopicUid = EXCLUDED.discussionPostDiscussionTopicUid, 
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

    var discussionPostTitle: String? = null

    var discussionPostMessage: String? = null

    var discussionPostStartDate: Long = 0

    var discussionPostDiscussionTopicUid: Long = 0

    var discussionPostVisible: Boolean = true

    var discussionPostArchive: Boolean = false

    //The person who started this post
    var discussionPostStartedPersonUid: Long = 0

    var discussionPostClazzUid: Long = 0

    @LastChangedTime
    @ReplicationVersionId
    var discussionPostLct: Long = 0

    companion object{
        const val TABLE_ID = 132
    }
}