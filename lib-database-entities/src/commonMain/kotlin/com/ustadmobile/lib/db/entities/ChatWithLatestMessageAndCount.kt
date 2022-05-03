package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ChatWithLatestMessageAndCount() : Chat() {

    var unreadMessageCount: Int = 0

    var latestMessage: String? = null

    var latestMessageTimestamp: Long = 0

    var otherPersonUid: Long = 0

    var otherPersonFirstNames: String? = null

    var otherPersonLastName: String? = null


    //Consider moving this logic away from the model
    val chatName: String?
        get() = if(chatGroup){
            chatTitle
        }else{
            var f = ""
            var l = ""
            if(otherPersonFirstNames != null){
                f = otherPersonFirstNames as String
            }
            if(otherPersonLastName != null){
                l = otherPersonLastName as String
            }

            "$f $l"
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ChatWithLatestMessageAndCount

        if (unreadMessageCount != other.unreadMessageCount) return false
        if (latestMessage != other.latestMessage) return false
        if (otherPersonUid != other.otherPersonUid) return false
        if (otherPersonFirstNames != other.otherPersonFirstNames) return false
        if (otherPersonLastName != other.otherPersonLastName) return false
        if (chatName != other.chatName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = unreadMessageCount
        result = 31 * result + (latestMessage?.hashCode() ?: 0)
        result = 31 * result + otherPersonUid.hashCode()
        result = 31 * result + (otherPersonFirstNames?.hashCode() ?: 0)
        result = 31 * result + (otherPersonLastName?.hashCode() ?: 0)
        result = 31 * result + (chatName?.hashCode() ?: 0)
        return result
    }


}
