package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzWithSchool: Clazz() {

    @Embedded
    var school: School? = null
}