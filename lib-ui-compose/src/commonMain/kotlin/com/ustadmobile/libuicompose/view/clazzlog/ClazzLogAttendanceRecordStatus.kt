package com.ustadmobile.libuicompose.view.clazzlog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.core.MR

val ATTENDANCE_STATUS_MAP = mapOf(
    ClazzLogAttendanceRecord.STATUS_ATTENDED to Pair(Icons.Default.Check, MR.strings.present),
    ClazzLogAttendanceRecord.STATUS_ABSENT to Pair(Icons.Default.Close, MR.strings.absent),
    ClazzLogAttendanceRecord.STATUS_PARTIAL to Pair(Icons.Default.Schedule, MR.strings.partial),
)