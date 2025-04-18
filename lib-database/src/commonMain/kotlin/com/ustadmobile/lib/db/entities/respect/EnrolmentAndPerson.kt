package com.ustadmobile.lib.db.entities.respect

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import kotlinx.serialization.Serializable

@Serializable
data class EnrolmentAndPerson(
    @Embedded
    var enrolment: ClazzEnrolment = ClazzEnrolment(),

    @Embedded
    var person: Person = Person(),

    @Embedded
    var picture: PersonPicture? = null,
)
