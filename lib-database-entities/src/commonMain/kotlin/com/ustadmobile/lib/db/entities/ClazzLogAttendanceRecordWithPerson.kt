package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEmbedded

class ClazzLogAttendanceRecordWithPerson : ClazzLogAttendanceRecord() {

    @UmEmbedded
    var person: Person? = null
}
