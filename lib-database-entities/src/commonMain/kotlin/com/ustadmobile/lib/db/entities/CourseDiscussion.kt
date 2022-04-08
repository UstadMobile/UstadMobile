package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CourseDiscussion.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = TABLE_ID , tracker = CourseDiscussionReplicate::class)
@Triggers(arrayOf(
    Trigger(
        name = "coursediscussion_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            """
                REPLACE INTO CourseDiscussion(courseDiscussionUid, 
                courseDiscussionStartDate, courseDiscussionTitle, 
                courseDiscussionDesc, courseDiscussionLct)
                VALUES(NEW.courseDiscussionUid, NEW.courseDiscussionStartDate, 
                NEW.courseDiscussionTitle, NEW.courseDiscussionDesc, NEW.courseDiscussionLct)
                /*psql ON CONFLICT (courseDiscussionUid) DO UPDATE 
                SET courseDiscussionTitle = EXCLUDED.courseDiscussionTitle, 
                courseDiscussionDesc = EXCLUDED.courseDiscussionDesc, 
                courseDiscussionStartDate = EXCLUDED.courseDiscussionStartDate, 
                courseDiscussionLct = EXCLUDED.courseDiscussionLct 
                */
            """
        ]
    )
))
open class CourseDiscussion() {

    @PrimaryKey(autoGenerate = true)
    var courseDiscussionUid: Long = 0

    var courseDiscussionTitle: String? = null

    var courseDiscussionDesc: String? = null

    var courseDiscussionStartDate: Long = 0

    @LastChangedTime
    @ReplicationVersionId
    var courseDiscussionLct: Long = 0

    companion object{
        const val TABLE_ID = 130
    }
}