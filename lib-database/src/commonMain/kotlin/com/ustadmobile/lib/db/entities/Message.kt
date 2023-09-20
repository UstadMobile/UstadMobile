package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Message.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
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
        sqlStatements = [
            TRIGGER_UPSERT_WHERE_NEWER
        ]
    )
))
open class Message() {

    @PrimaryKey(autoGenerate = true)
    var messageUid: Long = 0

    var messageSenderPersonUid: Long = 0

    var messageTableId: Int = 0

    var messageEntityUid: Long = 0

    var messageText: String? = null

    var messageTimestamp: Long = 0

    var messageClazzUid: Long = 0

    @ReplicateLastModified
    @ReplicateEtag
    var messageLct: Long = 0

    constructor(personUid: Long, table: Int, entityUid: Long, text: String, clazzUid: Long ) : this() {
        messageSenderPersonUid = personUid
        messageTableId = table
        messageEntityUid = entityUid
        messageText = text
        messageTimestamp = systemTimeInMillis()
        messageClazzUid = clazzUid
    }

    constructor(personUid: Long, table: Int, entityUid: Long, text: String) : this() {
        messageSenderPersonUid = personUid
        messageTableId = table
        messageEntityUid = entityUid
        messageText = text
        messageTimestamp = systemTimeInMillis()
    }

    companion object{
        const val TABLE_ID = 126
    }
}