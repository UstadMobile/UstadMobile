package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Chat.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
    Trigger(
        name = "chat_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = [TRIGGER_UPSERT],
    )
))
open class Chat() {

    @PrimaryKey(autoGenerate = true)
    var chatUid: Long = 0

    var chatStartDate: Long = 0

    var chatTitle: String? = null

    var chatGroup: Boolean = false

    constructor(title: String, isGroup: Boolean, startDate: Long):this(){
        chatTitle = title
        chatGroup = isGroup
        chatStartDate = startDate
    }

    constructor(title: String, isGroup: Boolean):this(){
        chatTitle = title
        chatGroup = isGroup
        chatStartDate = systemTimeInMillis()
    }

    @ReplicateLastModified
    @ReplicateEtag
    var chatLct: Long = 0

    companion object{
        const val TABLE_ID = 127
    }
}