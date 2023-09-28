package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CourseDiscussion.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = TABLE_ID ,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
    Trigger(
        name = "coursediscussion_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            TRIGGER_UPSERT_WHERE_NEWER
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

    @ReplicateLastModified
    @ReplicateEtag
    var courseDiscussionLct: Long = 0

    companion object{
        const val TABLE_ID = 130
    }
}