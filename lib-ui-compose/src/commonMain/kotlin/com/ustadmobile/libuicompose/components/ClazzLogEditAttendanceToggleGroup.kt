package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable

@Composable
expect fun ClazzLogEditAttendanceToggleGroup(

    isEnabled: Boolean,

    attendanceStatus: Int,

    onAttendanceStatusChanged: (Int) -> Unit

)