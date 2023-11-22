package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import kotlinx.serialization.Serializable

@Serializable
class ClazzEnrolmentAndPerson(
    @Embedded
    var person: Person? = null,
    @Embedded
    var enrolment: ClazzEnrolment? = null,
)
