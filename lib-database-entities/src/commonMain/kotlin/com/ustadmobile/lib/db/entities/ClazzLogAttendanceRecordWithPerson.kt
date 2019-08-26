package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import com.ustadmobile.lib.database.annotation.UmEmbedded

class ClazzLogAttendanceRecordWithPerson : ClazzLogAttendanceRecord() {

    @UmEmbedded
    @Embedded
    var person: Person? = null
}
