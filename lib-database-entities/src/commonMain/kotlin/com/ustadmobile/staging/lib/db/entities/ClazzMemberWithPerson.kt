package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing Person and ClazzMember
 */
@Serializable
class ClazzMemberWithPerson : ClazzMember() {

    @Embedded
    var person: Person? = null
}
