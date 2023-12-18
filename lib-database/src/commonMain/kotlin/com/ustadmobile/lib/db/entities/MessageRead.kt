package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.MessageRead.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = TABLE_ID ,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
    Trigger(
        name = "messageread_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = [TRIGGER_UPSERT],
    )
))
open class MessageRead() {

    @PrimaryKey(autoGenerate = true)
    var messageReadUid: Long = 0

    var messageReadPersonUid: Long = 0

    var messageReadMessageUid: Long = 0

    var messageReadEntityUid: Long = 0

    @ReplicateLastModified
    @ReplicateEtag
    var messageReadLct: Long = 0

    constructor(personUid: Long, messageUid: Long, entityUid: Long) : this() {
        messageReadPersonUid = personUid
        messageReadMessageUid = messageUid
        messageReadEntityUid = entityUid
    }

    companion object{
        const val TABLE_ID = 129
    }
}