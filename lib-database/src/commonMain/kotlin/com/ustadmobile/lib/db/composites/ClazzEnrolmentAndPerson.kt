package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import kotlinx.serialization.Serializable

@Serializable
data class ClazzEnrolmentAndPerson(
    @Embedded
    var person: Person? = null,
    @Embedded
    var enrolment: ClazzEnrolment? = null,
    @Embedded
    var picture: PersonPicture? = null,
)
