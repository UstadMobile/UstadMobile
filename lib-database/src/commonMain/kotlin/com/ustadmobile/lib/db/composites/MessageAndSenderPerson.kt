package com.ustadmobile.lib.db.composites

import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.lib.db.entities.Person
import kotlinx.serialization.Serializable

@Serializable
data class MessageAndSenderPerson(
    var message: Message? = null,
    var senderPerson: Person? = null,
)

