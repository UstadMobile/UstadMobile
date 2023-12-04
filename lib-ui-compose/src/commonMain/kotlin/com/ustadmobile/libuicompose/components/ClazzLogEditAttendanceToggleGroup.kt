package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.composites.PersonAndClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord

@Composable
expect fun ClazzLogEditAttendanceToggleGroup(

    isEnabled: Boolean,

    attendanceStatus: Int,

    onAttendanceStatusChanged: (Int) -> Unit

)