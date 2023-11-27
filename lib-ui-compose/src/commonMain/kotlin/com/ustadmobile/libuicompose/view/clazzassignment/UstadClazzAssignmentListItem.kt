package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.lib.db.entities.*
import java.util.*
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadHtmlText
import com.ustadmobile.libuicompose.util.compose.stringIdMapResource
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import com.ustadmobile.libuicompose.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewConstants.ASSIGNMENT_STATUS_MAP
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun UstadClazzAssignmentListItem(

    modifier: Modifier = Modifier,

    courseBlock: CourseBlockWithCompleteEntity,

    onClick: () -> Unit = { },

){

    val blockUiState = courseBlock.listItemUiState

    val assignment = courseBlock.assignment

    val assignmentUiState = assignment?.listItemUiState

    ListItem(
        modifier = modifier
            .clickable(onClick = onClick),
        leadingContent = {
            Icon(
                Icons.Default.AssignmentTurnedIn,
                contentDescription = "",
                modifier = Modifier.size(40.dp)
            )
        },
        headlineContent = { Text(courseBlock.cbTitle ?: "") },
        supportingContent = {
            Column{
                if (blockUiState.cbDescriptionVisible){
                    UstadHtmlText(
                        html = courseBlock.cbDescription ?: "",
                        htmlMaxLines = 1,
                    )
                }

                DateAndPointRow(courseBlock = courseBlock)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ){
                    if (assignmentUiState?.submissionStatusIconVisible == true){
                        Icon(
                            ASSIGNMENT_STATUS_MAP[assignment.fileSubmissionStatus]
                                ?: Icons.Default.DoneAll,
                            contentDescription = "")
                    }

                    if (assignmentUiState?.submissionStatusVisible == true){
                        Text(text = stringIdMapResource(
                            map = SubmissionConstants.STATUS_MAP,
                            key = assignment.fileSubmissionStatus)
                        )
                    }
                }
                
                if (assignmentUiState?.progressTextVisible == true){
                    Text(text = stringResource(MR.strings.three_num_items_with_name_with_comma,

                        assignment.progressSummary
                            ?.calculateNotSubmittedStudents() ?: 0,
                        stringResource(MR.strings.not_submitted_cap),

                        assignment.progressSummary?.submittedStudents ?: 0,
                        stringResource(MR.strings.submitted_cap),

                        assignment.progressSummary?.markedStudents ?: 0,
                        stringResource(MR.strings.marked_key)
                    ))
                }
            }
        }
    )
}

@Composable
private fun DateAndPointRow(
    courseBlock: CourseBlockWithCompleteEntity
){

    val blockUiState = courseBlock.listItemUiState

    val assignment = courseBlock.assignment

    val assignmentUiState = assignment?.listItemUiState

    val dateTime = rememberFormattedDateTime(
        timeInMillis = courseBlock.cbDeadlineDate,
        timeZoneId = TimeZone.getDefault().id
    )

    Row{
        if (blockUiState.cbDeadlineDateVisible){
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = "",
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = dateTime)
        }

        Spacer(modifier = Modifier.width(10.dp))

//To be fixed as part of the assignment screens
//        if (assignmentUiState?.assignmentMarkVisible == true){
//            Text("${assignment.mark?.camMark ?: 0}/" +
//                    "${courseBlock.cbMaxPoints} " +
//                    stringResource(id = R.string.points))
//        }
    }
}