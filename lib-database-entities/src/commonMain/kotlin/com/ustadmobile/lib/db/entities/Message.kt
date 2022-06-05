package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Message.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = TABLE_ID , tracker = MessageReplicate::class)
@Triggers(arrayOf(
    Trigger(
        name = "message_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            """
                REPLACE INTO Message(messageUid, messageSenderPersonUid, messageTableId, 
                messageEntityUid, messageText, messageTimestamp, messageClazzUid, messageLct)
                VALUES(NEW.messageUid, NEW.messageSenderPersonUid, NEW.messageTableId, 
                NEW.messageEntityUid, NEW.messageText, NEW.messageTimestamp, NEW.messageClazzUid, 
                NEW.messageLct)
                /*psql ON CONFLICT (messageUid) DO UPDATE 
                SET messageSenderPersonUid = EXCLUDED.messageSenderPersonUid, 
                messageTableId = EXCLUDED.messageTableId, 
                messageEntityUid = EXCLUDED.messageEntityUid, 
                messageText = EXCLUDED.messageText, messageTimestamp = EXCLUDED.messageTimestamp,
                messageClazzUid = EXCLUDED.messageClazzUid,
                messageLct = EXCLUDED.messageLct
                */
            """
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

    @LastChangedTime
    @ReplicationVersionId
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