package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.EnrolmentRequest
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture

data class EnrolmentRequestAndPersonDetails(
    @Embedded
    var enrolmentRequest: EnrolmentRequest? = null,

    @Embedded
    var personPicture: PersonPicture? = null,

    @Embedded
    var person: Person? = null,
)