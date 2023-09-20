package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(
    tableId = CourseGroupMember.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
    Trigger(
        name = "coursegroupmember_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            TRIGGER_UPSERT_WHERE_NEWER
        ]
    )
))
@Serializable
class CourseGroupMember {

    @PrimaryKey(autoGenerate = true)
    var cgmUid: Long = 0

    var cgmSetUid: Long = 0

    // real group numbers start from 1, 0 means this person is not yet in a group
    var cgmGroupNumber: Int = 0

    var cgmPersonUid: Long = 0

    @ReplicateLastModified
    @ReplicateEtag
    var cgmLct: Long = 0

    companion object {

        const val TABLE_ID = 243


    }

}