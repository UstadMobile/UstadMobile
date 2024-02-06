package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ChatMember.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
    Trigger(
        name = "chatmember_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = [ TRIGGER_UPSERT]
    )
))
class ChatMember() {

    @PrimaryKey(autoGenerate = true)
    var chatMemberUid: Long = 0

    var chatMemberChatUid: Long = 0

    var chatMemberPersonUid: Long = 0

    var chatMemberJoinedDate: Long = 0

    var chatMemberLeftDate: Long = Long.MAX_VALUE

    @ReplicateLastModified
    @ReplicateEtag
    var chatMemberLct: Long = 0


    constructor(chatUid: Long, personUid: Long):this(){
        chatMemberChatUid = chatUid
        chatMemberPersonUid = personUid
        chatMemberJoinedDate = systemTimeInMillis()
    }

    companion object{
        const val TABLE_ID = 128
    }
}