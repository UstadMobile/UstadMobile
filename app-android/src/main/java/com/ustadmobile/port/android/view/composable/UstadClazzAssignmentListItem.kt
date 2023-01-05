package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.toughra.ustadmobile.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.viewmodel.ClazzAssignmentUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.compose.messageIdMapResource
import com.ustadmobile.port.android.util.compose.rememberFormattedDateTime
import com.ustadmobile.port.android.view.ClazzAssignmentDetailOverviewFragment.Companion.ASSIGNMENT_STATUS_MAP

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadClazzAssignmentListItem(
    uiState: ClazzAssignmentUiState,
    onClickAssignment: (ClazzAssignmentWithMetrics?) -> Unit = {}
){

    ListItem(
        modifier = Modifier
            .paddingCourseBlockIndent(uiState.block.cbIndentLevel)
            .clickable {
                onClickAssignment(uiState.assignment)
            },

        icon = {
            Icon(
                Icons.Default.AssignmentTurnedIn,
                contentDescription = "",
                modifier = Modifier.size(40.dp)
            )
        },
        text = { Text(uiState.assignment.caTitle ?: "") },
        secondaryText = {
            Column{
                if (uiState.cbDescriptionVisible){
                    Text(text = uiState.block.cbDescription ?: "")
                }

                DateAndPointRow(uiState = uiState)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ){
                    if (uiState.submissionStatusIconVisible){
                        Icon(
                            painter = painterResource(
                                id = ASSIGNMENT_STATUS_MAP[uiState.assignment.fileSubmissionStatus]
                                    ?: R.drawable.ic_baseline_done_all_24),
                            contentDescription = "")
                    }

                    if (uiState.submissionStatusVisible){
                        Text(text = messageIdMapResource(
                            map = SubmissionConstants.STATUS_MAP,
                            key = uiState.assignment.fileSubmissionStatus)
                        )
                    }
                }
                
                if (uiState.progressTextVisible){
                    Text(text = stringResource(
                        id = R.string.three_num_items_with_name_with_comma,

                        uiState.assignment.progressSummary
                            ?.calculateNotSubmittedStudents() ?: 0,
                        stringResource(R.string.not_submitted_cap),

                        uiState.assignment.progressSummary?.submittedStudents ?: 0,
                        stringResource(R.string.submitted_cap),

                        uiState.assignment.progressSummary?.markedStudents ?: 0,
                        stringResource(R.string.marked)
                    ))
                }
            }
        }
    )
}

@Composable
fun DateAndPointRow(
    uiState: ClazzAssignmentUiState,
){

    val dateTime = rememberFormattedDateTime(
        timeInMillis = uiState.block.cbDeadlineDate,
        timeZoneId = uiState.timeZone)

    Row{
        if (uiState.cbDeadlineDateVisible){
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = "",
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = dateTime)
        }

        Spacer(modifier = Modifier.width(10.dp))

        if (uiState.assignmentMarkVisible){
            Text("${uiState.assignment.mark?.camMark ?: 0}/" +
                    "${uiState.block.cbMaxPoints} " +
                    stringResource(id = R.string.points))
        }
    }
}

@Composable
@Preview
private fun UstadClazzAssignmentListItemPreview() {
    val uiState = ClazzAssignmentUiState(
        assignment = ClazzAssignmentWithMetrics().apply {
            caTitle = "Module"
            mark = CourseAssignmentMark().apply {
                camPenalty = 20
                camMark = 20F
            }
            progressSummary = AssignmentProgressSummary().apply {
                hasMetricsPermission = true
            }
            fileSubmissionStatus = CourseAssignmentSubmission.MARKED
        },
        block = CourseBlockWithCompleteEntity().apply {
            cbDescription = "Description"
            cbDeadlineDate = 1672707505000
            cbMaxPoints = 100
            cbIndentLevel = 1
        }
    )
    UstadClazzAssignmentListItem(uiState)
}