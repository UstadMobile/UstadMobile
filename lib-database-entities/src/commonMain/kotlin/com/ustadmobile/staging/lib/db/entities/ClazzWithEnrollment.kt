package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ClazzWithEnrollment : Clazz() {

    var personUid: Long = 0

    var enrolled: Boolean? = null

    var numStudents: Int = 0
}
