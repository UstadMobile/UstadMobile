package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzEnrolmentWithClazz : ClazzEnrolment() {

    @Embedded
    var clazz: Clazz? = null

}