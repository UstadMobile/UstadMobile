package com.ustadmobile.libuicompose.view.clazzlog.editattendance

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceUiState
import com.ustadmobile.lib.db.composites.PersonAndClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.Person

@Composable
@Preview
fun ClazzLogEditAttendanceScreenPreview() {
    val uiState = ClazzLogEditAttendanceUiState(
        clazzLogAttendanceRecordList = listOf(
            PersonAndClazzLogAttendanceRecord(
                person = Person().apply {
                    firstNames = "Student Name"
                },
                attendanceRecord = ClazzLogAttendanceRecord().apply {
                    clazzLogAttendanceRecordUid = 0
                    attendanceStatus = ClazzLogAttendanceRecord.STATUS_ATTENDED
                }
            ),
            PersonAndClazzLogAttendanceRecord(
                person = Person().apply {
                    firstNames = "Student Name"
                },
                attendanceRecord = ClazzLogAttendanceRecord().apply {
                    clazzLogAttendanceRecordUid = 1
                    attendanceStatus = ClazzLogAttendanceRecord.STATUS_ATTENDED
                }
            ),
            PersonAndClazzLogAttendanceRecord(
                person = Person().apply {
                    firstNames = "Student Name"
                },
                attendanceRecord = ClazzLogAttendanceRecord().apply {
                    clazzLogAttendanceRecordUid = 2
                    attendanceStatus = ClazzLogAttendanceRecord.STATUS_ABSENT
                }
            )
        ),
        clazzLogsList = listOf(
            ClazzLog().apply {
                logDate = 1671629979000
            },
            ClazzLog().apply {
                logDate = 1655608510000
            },
            ClazzLog().apply {
                logDate = 1671975579000
            }
        )
    )

    ClazzLogEditAttendanceScreen(uiState)
}