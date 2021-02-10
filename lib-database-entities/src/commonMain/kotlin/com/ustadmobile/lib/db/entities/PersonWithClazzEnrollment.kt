package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonWithClazzEnrollmentDetails: Person() {

    var attendance: Float = 0f

    var earliestJoinDate: Long = 0L

    var latestDateLeft: Long = 0L

}