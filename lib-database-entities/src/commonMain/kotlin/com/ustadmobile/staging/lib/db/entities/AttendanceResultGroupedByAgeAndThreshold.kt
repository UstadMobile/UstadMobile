package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class AttendanceResultGroupedByAgeAndThreshold() {
    var total: Int = 0
    var gender: Int = 0
    var age: Int = 0
    var thresholdGroup: String? = null
}