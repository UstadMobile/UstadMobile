package com.ustadmobile.lib.db.composites

import com.ustadmobile.lib.db.entities.Message
import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    var conversationName: String? = null,
    var conversationPersonUid: Long = 0,
    var latestMessage: Message? = null,
)
