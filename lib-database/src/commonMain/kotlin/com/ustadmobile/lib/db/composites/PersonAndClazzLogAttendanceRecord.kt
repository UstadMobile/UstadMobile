package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import kotlinx.serialization.Serializable

@Serializable
data class PersonAndClazzLogAttendanceRecord(
    @Embedded
    var person: Person? = null,

    @Embedded
    var personPicture: PersonPicture? = null,

    @Embedded
    var attendanceRecord: ClazzLogAttendanceRecord? = null,
) {
}