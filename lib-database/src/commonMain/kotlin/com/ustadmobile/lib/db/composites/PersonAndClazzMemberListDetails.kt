package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import kotlinx.serialization.Serializable

@Serializable
data class PersonAndClazzMemberListDetails(
    @Embedded
    var person: Person? = null,

    @Embedded
    var personPicture: PersonPicture? = null,

    var earliestJoinDate: Long = 0L,

    var latestDateLeft: Long = 0L,

    var enrolmentRole: Int = 0,
)
