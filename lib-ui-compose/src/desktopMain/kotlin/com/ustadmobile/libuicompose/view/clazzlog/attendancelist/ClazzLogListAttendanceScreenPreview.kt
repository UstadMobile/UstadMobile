package com.ustadmobile.libuicompose.view.clazzlog.attendancelist

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.AttendanceGraphData
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceUiState
import com.ustadmobile.lib.db.entities.*

@Composable
@Preview
fun ClazzLogListAttendanceScreenPreview() {
    val uiStateVal = ClazzLogListAttendanceUiState(
//        clazzLogsList = {
//            ListPagingSource(listOf(
//                ClazzLog().apply {
//                    clazzLogUid = 1
//                    clazzLogNumPresent = 40
//                    clazzLogNumPartial = 15
//                    clazzLogNumAbsent = 10
//                    logDate = 1675089491000
//                },
//                ClazzLog().apply {
//                    clazzLogUid = 2
//                    clazzLogNumPresent = 40
//                    clazzLogNumPartial = 30
//                    clazzLogNumAbsent = 30
//                    logDate = 1675003091000
//                },
//                ClazzLog().apply {
//                    clazzLogUid = 3
//                    clazzLogNumPresent = 70
//                    clazzLogNumPartial = 20
//                    clazzLogNumAbsent = 2
//                    logDate = 1674916691000
//                }
//            ))
//        },
        graphData = AttendanceGraphData(
             percentageAttendedSeries = listOf(
                 Pair(1674743891000, .80f),
                 Pair(1674830291000, .70f),
                 Pair(1674916691000, .50f),
                 Pair(1675003091000, .40f),
                 Pair(1675089491000, .15f),
             ),
            percentageLateSeries = listOf(
                Pair(1674743891000, .15f),
                Pair(1674830291000, .20f),
                Pair(1674916691000, .10f),
                Pair(1675003091000, .30f),
                Pair(1675089491000, .60f),
            ),
            graphDateRange = Pair(1674743891000, 1675089491000),
        )
    )
    ClazzLogListAttendanceScreen(uiStateVal)
}