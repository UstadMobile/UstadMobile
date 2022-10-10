package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ChatMember.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = TABLE_ID , tracker = ChatMemberReplicate::class,
    priority = ReplicateEntity.HIGHEST_PRIORITY )
@Triggers(arrayOf(
    Trigger(
        name = "chatmember_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            """
                REPLACE INTO ChatMember(chatMemberUid, chatMemberChatUid, chatMemberPersonUid, 
                chatMemberJoinedDate, chatMemberLeftDate, chatMemberLct)
                VALUES(NEW.chatMemberUid, NEW.chatMemberChatUid, NEW.chatMemberPersonUid, 
                NEW.chatMemberJoinedDate, NEW.chatMemberLeftDate, NEW.chatMemberLct)
                /*psql ON CONFLICT (chatMemberUid) DO UPDATE 
                SET chatMemberChatUid = EXCLUDED.chatMemberChatUid, 
                chatMemberPersonUid = EXCLUDED.chatMemberPersonUid,
                chatMemberJoinedDate = EXCLUDED.chatMemberJoinedDate, 
                chatMemberLeftDate = EXCLUDED.chatMemberLeftDate, 
                chatMemberLct = EXCLUDED.chatMemberLct
                
                */
            """
        ]
    )
))
class ChatMember() {

    @PrimaryKey(autoGenerate = true)
    var chatMemberUid: Long = 0

    var chatMemberChatUid: Long = 0

    var chatMemberPersonUid: Long = 0

    var chatMemberJoinedDate: Long = 0

    var chatMemberLeftDate: Long = Long.MAX_VALUE

    @LastChangedTime
    @ReplicationVersionId
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