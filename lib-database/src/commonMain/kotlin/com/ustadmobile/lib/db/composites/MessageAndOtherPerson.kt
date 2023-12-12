package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.lib.db.entities.Person
import kotlinx.serialization.Serializable

@Serializable
data class MessageAndOtherPerson(
    @Embedded
    var message: Message? = null,
    @Embedded
    var otherPerson: Person? = null,
)
