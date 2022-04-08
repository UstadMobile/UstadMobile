package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CourseDiscussionTopic.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = TABLE_ID , tracker = CourseDiscussionTopicReplicate::class)
@Triggers(arrayOf(
    Trigger(
        name = "coursediscussiontopic_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            """
                REPLACE INTO CourseDiscussionTopic(courseDiscussionTopicUid, 
                courseDiscussionTopicStartedPersonUid, courseDiscussionTopicTitle, 
                courseDiscussionTopicStartDate, courseDiscussionTopicLct)
                VALUES(NEW.courseDiscussionTopicUid, NEW.courseDiscussionTopicStartedPersonUid, 
                NEW.courseDiscussionTopicTitle, NEW.courseDiscussionTopicStartDate, 
                NEW.courseDiscussionTopicLct)
                /*psql ON CONFLICT (courseDiscussionTopicUid) DO UPDATE 
                SET courseDiscussionTopicStartedPersonUid = EXCLUDED.courseDiscussionTopicStartedPersonUid, 
                courseDiscussionTopicTitle = EXCLUDED.courseDiscussionTopicTitle, 
                courseDiscussionTopicStartDate = EXCLUDED.courseDiscussionTopicStartDate, 
                courseDiscussionTopicLct = EXCLUDED.courseDiscussionTopicLct 
                */
            """
        ]
    )
))
open class CourseDiscussionTopic() {

    @PrimaryKey(autoGenerate = true)
    var courseDiscussionTopicUid: Long = 0

    var courseDiscussionTopicStartedPersonUid: Long = 0

    var courseDiscussionTopicTitle: String? = null

    var courseDiscussionTopicStartDate: Long = 0

    @LastChangedTime
    @ReplicationVersionId
    var courseDiscussionTopicLct: Long = 0

    companion object{
        const val TABLE_ID = 132
    }
}