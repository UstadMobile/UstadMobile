package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord

@ShallowCopy
expect fun ClazzLogAttendanceRecord.shallowCopy(
    block: ClazzLogAttendanceRecord.() -> Unit
): ClazzLogAttendanceRecord