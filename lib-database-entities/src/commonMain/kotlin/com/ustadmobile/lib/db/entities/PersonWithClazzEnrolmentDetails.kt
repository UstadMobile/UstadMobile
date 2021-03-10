package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonWithClazzEnrolmentDetails: Person() {

    var attendance: Float = 0f

    var earliestJoinDate: Long = 0L

    var latestDateLeft: Long = 0L

    var enrolmentRole: Int = 0

}