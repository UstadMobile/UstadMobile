package com.ustadmobile.libuicompose.view.clazzassignment

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
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.lib.db.entities.*
import java.util.*
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterialApi::class)
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
        icon = {
            Icon(
                Icons.Default.AssignmentTurnedIn,
                contentDescription = "",
                modifier = Modifier.size(40.dp)
            )
        },
        text = { Text(courseBlock.cbTitle ?: "") },
        secondaryText = {
            Column{
                if (blockUiState.cbDescriptionVisible){
                    // TODO error
//                    HtmlText(
//                        html = courseBlock.cbDescription ?: "",
//                        htmlMaxLines = 1,
//                    )
                }

                DateAndPointRow(courseBlock = courseBlock)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ){
                    if (assignmentUiState?.submissionStatusIconVisible == true){
//                        Icon(
//                            painter = painterResource(
//                                     // TODO error
//                                id = ASSIGNMENT_STATUS_MAP[assignment.fileSubmissionStatus]
//                                    ?: R.drawable.ic_baseline_done_all_24),
//                            contentDescription = "")
                    }

                    if (assignmentUiState?.submissionStatusVisible == true){
                                      // TODO error
//                        Text(text = stringIdMapResource(
//                            map = SubmissionConstants.STATUS_MAP,
//                            key = assignment.fileSubmissionStatus)
//                        )
                    }
                }
                
                if (assignmentUiState?.progressTextVisible == true){
                    Text(text = stringResource(MR.strings.three_num_items_with_name_with_comma,

                        assignment.progressSummary
                            // TODO error
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

                    // TODO error
//    val dateTime = rememberFormattedDateTime(
//        timeInMillis = courseBlock.cbDeadlineDate,
//        timeZoneId = TimeZone.getDefault().id
//    )

    Row{
        if (blockUiState.cbDeadlineDateVisible){
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = "",
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(2.dp))
//            Text(text = dateTime)
        }

        Spacer(modifier = Modifier.width(10.dp))
        // TODO error
//To be fixed as part of the assignment screens
//        if (assignmentUiState?.assignmentMarkVisible == true){
//            Text("${assignment.mark?.camMark ?: 0}/" +
//                    "${courseBlock.cbMaxPoints} " +
//                    stringResource(id = R.string.points))
//        }
    }
}