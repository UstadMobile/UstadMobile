package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzLogAttendanceRecordWithPerson : ClazzLogAttendanceRecord() {

    @Embedded
    var person: Person? = null
}
