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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.toughra.ustadmobile.R
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.viewmodel.ClazzAssignmentUiState
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseBlockWithCompleteEntity
import com.ustadmobile.port.android.util.compose.messageIdMapResource
import com.ustadmobile.port.android.util.compose.rememberFormattedDate
import com.ustadmobile.port.android.view.ClazzAssignmentDetailOverviewFragment.Companion.ASSIGNMENT_STATUS_MAP

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadClazzAssignmentListItem(
    uiState: ClazzAssignmentUiState,
    onClickAssignment: (ClazzAssignmentWithMetrics?) -> Unit = {}
){

    ListItem(
        modifier = Modifier.clickable {
            onClickAssignment(uiState.assignment)
        },
        icon = {
            Row {
                Spacer(modifier = Modifier
                    .paddingCourseBlockIndent(uiState.block?.cbIndentLevel))
                Icon(
                    Icons.Default.AssignmentTurnedIn,
                    contentDescription = "",
                )
            }
        },
        text = { Text(uiState.assignment?.caTitle ?: "") },
        secondaryText = {
            Column{
                if (uiState.cbDescriptionVisible){
                    Text(text = uiState.block?.cbDescription ?: "")
                }

                DateAndPointRow(uiState = uiState)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ){
                    if (uiState.submissionStatusIconVisible){
                        Icon(
                            painter = painterResource(
                                id = ASSIGNMENT_STATUS_MAP[uiState.assignment?.fileSubmissionStatus]
                                    ?: R.drawable.ic_baseline_done_all_24),
                            contentDescription = "")
                    }

                    if (uiState.submissionStatusVisible){
                        Text(text = messageIdMapResource(
                            map = SubmissionConstants.STATUS_MAP,
                            key = uiState.assignment?.fileSubmissionStatus
                                ?: MessageID.marked_cap)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun DateAndPointRow(
    uiState: ClazzAssignmentUiState,
){

    val dateTime = rememberFormattedDate(
        timeInMillis = uiState.block?.cbDeadlineDate ?: 0,
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

        Spacer(modifier = Modifier.width(5.dp))

        if (uiState.assignmentMarkVisible){
            Text(buildAnnotatedString {
                append(
                    "${uiState.assignment?.mark?.camMark ?: 0}/" +
                            "${uiState.block?.cbMaxPoints ?: 0 } " +
                            stringResource(id = R.string.points)
                )
                withStyle(style = SpanStyle(color = Color.Red)) {
                    if (uiState.assignmentPenaltyVisible){
                        append(stringResource(
                            id = R.string.late_penalty,
                            uiState.block?.cbLateSubmissionPenalty ?: 0
                        ))
                    }
                }
            })
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
                hasMetricsPermission = false
            }
            fileSubmissionStatus = 2
        },
        block = CourseBlockWithCompleteEntity().apply {
            cbDescription = "Description"
            cbDeadlineDate = 1672707505000
            cbMaxPoints = 100
        }
    )
    UstadClazzAssignmentListItem(uiState)
}