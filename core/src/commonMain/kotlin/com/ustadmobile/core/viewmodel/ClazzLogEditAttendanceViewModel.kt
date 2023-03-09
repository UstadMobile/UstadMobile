package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson

data class ClazzLogEditAttendanceUiState(

    val clazzLogAttendanceRecordList: List<ClazzLogAttendanceRecordWithPerson> = emptyList(),

    val clazzLogTimezone: String = "UTC",

    val clazzLogsList: List<ClazzLog> = emptyList(),

    val fieldsEnabled: Boolean = true,

    val timeZone: String = "UTC"

)