package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.controller.ClazzLogListAttendancePresenter
import com.ustadmobile.lib.db.entities.ClazzLog

data class ClazzLogListAttendanceUiState(

    var clazzTimeZone: String = "UTC",

    /**
     * The data that will be used to draw the chart. This is a list with pairs that represent the
     * x/y coordinates. The x is the timestamp, the y is the attendance (between 0 and 100)
     */
    var graphData: AttendanceGraphData? = null,

    var recordAttendanceOptions: List<ClazzLogListAttendancePresenter.RecordAttendanceOption> =
        emptyList(),

    var clazzLogsList: List<ClazzLog> = emptyList()

)

data class AttendanceGraphData(

    val percentageAttendedSeries: List<Pair<Long, Float>>,

    val percentageLateSeries: List<Pair<Long, Float>>,

    val graphDateRange: Pair<Long, Long>
)