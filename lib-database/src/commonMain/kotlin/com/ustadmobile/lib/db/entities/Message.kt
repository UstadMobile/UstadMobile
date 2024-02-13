package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Message.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity(
    indices = arrayOf(
        Index("messageSenderPersonUid", "messageToPersonUid", "messageTimestamp", name = "message_idx_send_to_time")
    )
)
@Serializable
@ReplicateEntity(
    tableId = TABLE_ID ,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
    Trigger(
        name = "message_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = [TRIGGER_UPSERT],
    )
))
data class Message(
    @PrimaryKey(autoGenerate = true)
    var messageUid: Long = 0,

    var messageSenderPersonUid: Long = 0,

    var messageToPersonUid: Long = 0,

    var messageText: String? = null,

    var messageTimestamp: Long = 0,

    @ReplicateLastModified
    @ReplicateEtag
    var messageLct: Long = 0,
) {



    companion object{
        const val TABLE_ID = 126
    }
}