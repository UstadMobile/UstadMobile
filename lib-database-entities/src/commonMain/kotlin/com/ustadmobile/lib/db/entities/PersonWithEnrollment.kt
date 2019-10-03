package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonWithEnrollment : Person() {

    var clazzUid: Long = 0

    var enrolled: Boolean? = null

    var attendancePercentage: Float = 0.toFloat()

    var clazzMemberRole: Int = 0

    var clazzName: String? = null

    var personPictureUid: Long = 0
}
