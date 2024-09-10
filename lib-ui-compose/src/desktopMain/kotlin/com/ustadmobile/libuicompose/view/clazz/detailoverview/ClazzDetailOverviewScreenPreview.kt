package com.ustadmobile.libuicompose.view.clazz.detailoverview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewUiState
import com.ustadmobile.lib.db.composites.ClazzAndDisplayDetails
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Schedule

@Composable
@Preview
fun ClazzDetailOverviewScreenPreview() {
    ClazzDetailOverviewScreen(
        uiState = ClazzDetailOverviewUiState(
            clazzAndDetail = ClazzAndDisplayDetails(
                clazz = Clazz().apply {
                    clazzDesc = "Description"
                    clazzCode = "abc123"
                    clazzSchoolUid = 1
                    clazzStartTime = 1682074513000
                    clazzEndTime = 1713682513000
                }
            ),
            scheduleList = listOf(
                Schedule().apply {
                    scheduleUid = 1
                    sceduleStartTime = 0
                    scheduleEndTime = 0
                    scheduleFrequency = Schedule.SCHEDULE_FREQUENCY_WEEKLY
                    scheduleDay = Schedule.DAY_SUNDAY
                },
                Schedule().apply {
                    scheduleUid = 2
                    sceduleStartTime = 0
                    scheduleEndTime = 0
                    scheduleFrequency = Schedule.SCHEDULE_FREQUENCY_WEEKLY
                    scheduleDay = Schedule.DAY_MONDAY
                }
            ),
//            courseBlockList = {
//                ListPagingSource(
//                    listOf(
//                        CourseBlockWithCompleteEntity().apply {
//                            cbUid = 1
//                            cbTitle = "Module"
//                            cbDescription = "Description"
//                            cbType = CourseBlock.BLOCK_MODULE_TYPE
//                        },
//                        CourseBlockWithCompleteEntity().apply {
//                            cbUid = 2
//                            cbTitle = "Main discussion board"
//                            cbType = CourseBlock.BLOCK_DISCUSSION_TYPE
//                        },
//                        CourseBlockWithCompleteEntity().apply {
//                            cbUid = 3
//                            cbDescription = "Description"
//                            cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
//                            assignment = ClazzAssignmentWithMetrics().apply {
//                                caTitle = "Assignment"
//                                fileSubmissionStatus = CourseAssignmentSubmission.NOT_SUBMITTED
//                                progressSummary = AssignmentProgressSummary().apply {
//                                    submittedStudents = 5
//                                    markedStudents = 10
//                                }
//                            }
//                        },
//                        CourseBlockWithCompleteEntity().apply {
//                            cbUid = 4
//                            cbType = CourseBlock.BLOCK_CONTENT_TYPE
//                            entry = ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
//                                title = "Content Entry"
//                                scoreProgress = ContentEntryStatementScoreProgress().apply {
//                                    success = StatementEntity.RESULT_SUCCESS
//                                    progress = 70
//                                }
//                            }
//                        },
//                        CourseBlockWithCompleteEntity().apply {
//                            cbUid = 5
//                            cbTitle = "Text Block Module"
//                            cbDescription = "<pre>\n" +
//                                    "            GeeksforGeeks\n" +
//                                    "                         A Computer   Science Portal   For Geeks\n" +
//                                    "        </pre>"
//                            cbType = CourseBlock.BLOCK_TEXT_TYPE
//                        }
//                    )
//                )
//            },
            clazzCodeVisible = true
        )
    )
}