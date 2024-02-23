package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.EnrolmentRequest
import com.ustadmobile.lib.db.entities.PersonPicture

data class EnrolmentRequestAndPersonPicture(
    @Embedded
    var enrolmentRequest: EnrolmentRequest? = null,

    @Embedded
    var personPicture: PersonPicture? = null
)