package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing Person and ClazzEnrollment
 */
@Serializable
class ClazzEnrollmentWithPerson : ClazzEnrollment() {

    @Embedded
    var person: Person? = null
}
