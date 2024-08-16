package com.ustadmobile.libuicompose.view.clazz.edit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditUiState
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndAndTerminology
import com.ustadmobile.lib.db.entities.Schedule

@Composable
@Preview
fun ClazzEditScreenPreview() {
    val uiState: ClazzEditUiState by remember {
        mutableStateOf(
            ClazzEditUiState(
                entity = ClazzWithHolidayCalendarAndAndTerminology().apply {

                },
                clazzSchedules = listOf(
                    Schedule().apply {
                        sceduleStartTime = 0
                        scheduleEndTime = 0
                        scheduleFrequency = Schedule.SCHEDULE_FREQUENCY_WEEKLY
                        scheduleDay = Schedule.DAY_SUNDAY
                    }
                ),
                courseBlockList = listOf(),
            )
        )
    }


    ClazzEditScreen(
        uiState = uiState,
        onMoveCourseBlock = { _, _ ->

        }
    )


}