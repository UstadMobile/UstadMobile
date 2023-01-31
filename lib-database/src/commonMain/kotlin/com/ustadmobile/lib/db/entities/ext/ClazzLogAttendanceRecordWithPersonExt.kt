package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson

@ShallowCopy
expect fun ClazzLogAttendanceRecordWithPerson.shallowCopy(
    block: ClazzLogAttendanceRecordWithPerson.() -> Unit
): ClazzLogAttendanceRecordWithPerson