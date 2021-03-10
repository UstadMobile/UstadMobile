package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing Person and ClazzEnrolment
 */
@Serializable
class ClazzEnrolmentWithPerson : ClazzEnrolment() {

    @Embedded
    var person: Person? = null
}
