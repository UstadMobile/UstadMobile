package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ClazzEnrolmentWithClazzAndAttendance : ClazzEnrolmentWithClazz() {

    var attendance: Float = 0f

}