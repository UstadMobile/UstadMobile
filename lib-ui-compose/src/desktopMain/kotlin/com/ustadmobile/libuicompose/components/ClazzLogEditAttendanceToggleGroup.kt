package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord

private val iconsMap = mapOf(
    ClazzLogAttendanceRecord.STATUS_ATTENDED to Icons.Default.Done,
    ClazzLogAttendanceRecord.STATUS_ABSENT to Icons.Default.Close,
    ClazzLogAttendanceRecord.STATUS_PARTIAL to Icons.Default.Schedule
)

@Composable
actual fun ClazzLogEditAttendanceToggleGroup(
    isEnabled: Boolean,
    attendanceStatus: Int,
    onAttendanceStatusChanged: (Int) -> Unit
) {

    Row {

        iconsMap.forEach { (status, icon) ->

            if(attendanceStatus==status){
                FilledIconButton(
                    onClick = { onAttendanceStatusChanged(status) },
                    content = {
                        Icon(
                            icon ?: Icons.Default.Close,
                            contentDescription = "ClazzLogAttendanceStatusButton"
                        )
                    },
                    enabled = isEnabled
                )
            } else {
                OutlinedIconButton(
                    onClick = { onAttendanceStatusChanged(status) },
                    content = {
                        Icon(
                            icon ?: Icons.Default.Done,
                            contentDescription = "ClazzLogAttendanceStatusButton"
                        )
                    },
                    enabled = isEnabled
                )
            }
        }

    }
}