package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing Message and Person
 */
@Serializable
class MessageWithPerson : Message() {
    @Embedded
    var messagePerson: Person ? = null

    @Embedded
    var messageRead: MessageRead? = null


}
