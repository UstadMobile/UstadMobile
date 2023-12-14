package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
data class PersonParentJoinAndMinorPerson(
    @Embedded
    var personParentJoin: PersonParentJoin? = null,
    @Embedded
    var minorPerson: Person? = null
)
