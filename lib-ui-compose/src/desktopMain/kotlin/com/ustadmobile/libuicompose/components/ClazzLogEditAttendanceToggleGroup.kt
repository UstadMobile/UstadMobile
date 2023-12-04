package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

            IconButton(
                modifier = Modifier.border( width = 2.dp,
                    color = MaterialTheme.colors.primary,
                    shape = RoundedCornerShape(5.dp)
                ).background(color = if((attendanceStatus+1)==status) MaterialTheme.colors.onSurface else Color.Green),
                onClick = { onAttendanceStatusChanged(status) },
                content = {
                    Icon(
                        icon ?: Icons.Default.Done,
                        contentDescription = null
                    )
                },
                enabled = isEnabled
            )
        }

    }
}