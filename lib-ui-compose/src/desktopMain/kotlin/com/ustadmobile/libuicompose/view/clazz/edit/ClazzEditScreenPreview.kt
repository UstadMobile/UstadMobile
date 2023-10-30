package com.ustadmobile.libuicompose.view.clazz.edit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.composable.*
import org.burnoutcrew.reorderable.*
import java.util.*

@Composable
@Preview
fun ClazzEditScreenPreview() {
    var uiState: ClazzEditUiState by remember {
        mutableStateOf(
            ClazzEditUiState(
                entity = ClazzWithHolidayCalendarAndSchoolAndTerminology().apply {

                },
                clazzSchedules = listOf(
                    Schedule().apply {
                        sceduleStartTime = 0
                        scheduleEndTime = 0
                        scheduleFrequency = Schedule.SCHEDULE_FREQUENCY_WEEKLY
                        scheduleDay = Schedule.DAY_SUNDAY
                    }
                ),
                courseBlockList = listOf(
                    CourseBlockWithEntity().apply {
                        cbUid = 1000
                        cbTitle = "Module"
                        cbHidden = true
                        cbType = CourseBlock.BLOCK_MODULE_TYPE
                        cbIndentLevel = 0
                    },
                    CourseBlockWithEntity().apply {
                        cbUid = 1001
                        cbTitle = "Content"
                        cbHidden = false
                        cbType = CourseBlock.BLOCK_CONTENT_TYPE
                        entry = ContentEntry().apply {
                            contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                        }
                        cbIndentLevel = 1
                    },
                    CourseBlockWithEntity().apply {
                        cbUid = 1002
                        cbTitle = "Assignment"
                        cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                        cbHidden = false
                        cbIndentLevel = 1
                    },
                ),
            )
        )
    }


    ClazzEditScreen(
        uiState = uiState,
        onMoveCourseBlock = { fromIndex, toIndex ->
            uiState = uiState.copy(
                courseBlockList = uiState.courseBlockList.toMutableList().apply {
                    val swapFromIndex = indexOfFirst { it.cbUid == fromIndex.key }
                    val swapToIndex = indexOfFirst { it.cbUid == toIndex.key }
                    Collections.swap(this, swapFromIndex, swapToIndex)
                }.toList()
            )
        }
    )


}