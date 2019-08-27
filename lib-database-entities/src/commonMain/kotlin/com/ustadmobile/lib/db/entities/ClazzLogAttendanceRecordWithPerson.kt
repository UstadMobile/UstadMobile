package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class ClazzLogAttendanceRecordWithPerson : ClazzLogAttendanceRecord() {

    @Embedded
    var person: Person? = null
}
