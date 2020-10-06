package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing Person and LearnerGroupMember
 */
@Serializable
class LearnerGroupMemberWithPerson : LearnerGroupMember() {

    @Embedded
    var person: Person? = null
}