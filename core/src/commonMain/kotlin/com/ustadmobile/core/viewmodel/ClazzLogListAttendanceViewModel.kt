package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.controller.ClazzLogListAttendancePresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ClazzLog

data class ClazzLogListAttendanceUiState(

    /**
     * The data that will be used to draw the chart. This is a list with pairs that represent the
     * x/y coordinates. The x is the timestamp, the y is the attendance (between 0 and 100)
     */
    val graphData: AttendanceGraphData? = null,

    val recordAttendanceOptions: List<ClazzLogListAttendancePresenter.RecordAttendanceOption> =
        emptyList(),

    val clazzLogsList: List<ClazzLog> = emptyList(),

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = 7,

    val viewIdToNumDays: List<MessageIdOption2> = listOf(
        MessageIdOption2(MessageID.last_week, 7),
        MessageIdOption2(MessageID.last_month, 30),
        MessageIdOption2(MessageID.last_three_months, 90)
    ),
)

data class AttendanceGraphData(

    val percentageAttendedSeries: List<Pair<Long, Float>>,

    val percentageLateSeries: List<Pair<Long, Float>>,

    val graphDateRange: Pair<Long, Long>
)