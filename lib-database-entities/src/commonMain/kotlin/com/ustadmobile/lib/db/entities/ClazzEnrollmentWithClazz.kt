package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzEnrollmentWithClazz : ClazzEnrollment() {

    @Embedded
    var clazz: Clazz? = null

}