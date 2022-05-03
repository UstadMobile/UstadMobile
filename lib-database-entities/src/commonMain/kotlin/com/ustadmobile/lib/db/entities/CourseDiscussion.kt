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
                courseDiscussionActive, courseDiscussionTitle, 
                courseDiscussionDesc, courseDiscussionClazzUid,  courseDiscussionLct)
                VALUES(NEW.courseDiscussionUid, NEW.courseDiscussionActive,
                NEW.courseDiscussionTitle, NEW.courseDiscussionDesc, NEW.courseDiscussionClazzUid,
                 NEW.courseDiscussionLct)
                /*psql ON CONFLICT (courseDiscussionUid) DO UPDATE 
                SET courseDiscussionActive = EXCLUDED.courseDiscussionActive, 
                courseDiscussionTitle = EXCLUDED.courseDiscussionTitle, 
                courseDiscussionDesc = EXCLUDED.courseDiscussionDesc, 
                courseDiscussionClazzUid = EXCLUDED.courseDiscussionClazzUid,
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

    var courseDiscussionClazzUid: Long = 0

    var courseDiscussionActive: Boolean = true

    @LastChangedTime
    @ReplicationVersionId
    var courseDiscussionLct: Long = 0

    companion object{
        const val TABLE_ID = 130
    }
}