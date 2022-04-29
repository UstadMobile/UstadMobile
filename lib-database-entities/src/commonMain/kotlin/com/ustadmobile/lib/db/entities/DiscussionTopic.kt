package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.DiscussionTopic.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = TABLE_ID , tracker = DiscussionTopicReplicate::class)
@Triggers(arrayOf(
    Trigger(
        name = "discussiontopic_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            """
                REPLACE INTO DiscussionTopic(discussionTopicUid, 
                discussionTopicTitle, discussionTopicDesc, 
                discussionTopicStartDate, discussionTopicCourseDiscussionUid,
                discussionTopicVisible, discussionTopicArchive,
                discussionTopicIndex, discussionTopicClazzUid, discussionTopicLct)
                VALUES(NEW.discussionTopicUid, 
                NEW.discussionTopicTitle, NEW.discussionTopicDesc, 
                NEW.discussionTopicStartDate, NEW.discussionTopicCourseDiscussionUid,
                NEW.discussionTopicVisible, NEW.discussionTopicArchive,
                 NEW.discussionTopicIndex, NEW.discussionTopicClazzUid, NEW.discussionTopicLct)
                /*psql ON CONFLICT (discussionTopicUid) DO UPDATE 
                SET discussionTopicTitle = EXCLUDED.discussionTopicTitle, 
                discussionTopicDesc = EXCLUDED.discussionTopicDesc, 
                discussionTopicStartDate = EXCLUDED.discussionTopicStartDate, 
                discussionTopicCourseDiscussionUid = EXCLUDED.discussionTopicCourseDiscussionUid, 
                discussionTopicVisible = EXCLUDED.discussionTopicVisible, 
                discussionTopicArchive = EXCLUDED.discussionTopicArchive,
                discussionTopicIndex = EXCLUDED.discussionTopicIndex,
                discussionTopicClazzUid = EXCLUDED.discussionTopicClazzUid,
                discussionTopicLct = EXCLUDED.discussionTopicLct
                
                */
            """
        ]
    )
))
open class DiscussionTopic() {

    @PrimaryKey(autoGenerate = true)
    var discussionTopicUid: Long = 0

    var discussionTopicTitle: String? = null

    var discussionTopicDesc: String? = null

    var discussionTopicStartDate: Long = 0

    var discussionTopicCourseDiscussionUid: Long = 0

    var discussionTopicVisible: Boolean = true

    var discussionTopicArchive: Boolean = false

    var discussionTopicIndex: Int = 0

    var discussionTopicClazzUid: Long = 0

    @LastChangedTime
    @ReplicationVersionId
    var discussionTopicLct: Long = 0

    companion object{
        const val TABLE_ID = 131
    }
}