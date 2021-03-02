package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
open class ClazzEnrolmentWithClazz : ClazzEnrolment() {

    @Embedded
    var clazz: Clazz? = null

}