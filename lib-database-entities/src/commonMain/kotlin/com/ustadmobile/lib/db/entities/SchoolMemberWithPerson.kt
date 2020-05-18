package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing Person and ClazzMember
 */
@Serializable
class SchoolMemberWithPerson : SchoolMember() {

    @Embedded
    var person: Person? = null
}
