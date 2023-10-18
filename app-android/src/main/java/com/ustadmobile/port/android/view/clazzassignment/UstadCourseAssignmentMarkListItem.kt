package com.ustadmobile.port.android.view.clazzassignment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.ext.penaltyPercentage
import com.ustadmobile.core.util.ext.roundTo
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import java.util.TimeZone
import com.ustadmobile.core.R as CR

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadCourseAssignmentMarkListItem(
    uiState: UstadCourseAssignmentMarkListItemUiState,
    modifier: Modifier = Modifier,
){

    var text = uiState.markerName

    if (uiState.markerGroupNameVisible){
        text += "  (${stringResource(CR.string.group_number, uiState.peerGroupNumber)})"
    }

    val formattedTime = rememberFormattedDateTime(
        timeInMillis = uiState.mark.courseAssignmentMark?.camLct ?: 0,
        timeZoneId = TimeZone.getDefault().id,
        joinDateAndTime = { date, time -> "$date\n$time" }
    )

    ListItem(
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_person_black_24dp),
                contentDescription = "",
                modifier = Modifier.size(40.dp)
            )
        },
        text = { Text(text) },
        secondaryText = {
            Column {
                Row {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = "",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        buildAnnotatedString {
                            append("${uiState.mark.courseAssignmentMark?.camMark?.roundTo(2)}")
                            append("/${uiState.mark.courseAssignmentMark?.camMaxMark?.roundTo(2)}")
                            append(" ${stringResource(CR.string.points)}")

                            if (uiState.camPenaltyVisible){
                                withStyle(style = SpanStyle(color = colorResource(R.color.errorColor)))
                                {
                                    append(" ")
                                    append(
                                        stringResource(CR.string.late_penalty,
                                            uiState.mark.courseAssignmentMark?.penaltyPercentage() ?: 0)
                                    )
                                }
                            }
                        }
                    )
                }

                Row {
                    Icon(
                        Icons.Filled.Chat,
                        contentDescription = "",
                        modifier = Modifier.size(16.dp)
                    )

                    Text(uiState.mark.courseAssignmentMark?.camMarkerComment ?: "")
                }

            }
        },
        trailing = {
            Text(formattedTime)
        }
    )
}

@Composable
@Preview
private fun UstadMarksPersonListItemPreview() {
    UstadCourseAssignmentMarkListItem(
        uiState = UstadCourseAssignmentMarkListItemUiState(
            mark = CourseAssignmentMarkAndMarkerName(
                courseAssignmentMark = CourseAssignmentMark().apply {
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                    camMark = 8.1f
                    camPenalty = 0.9f
                    camMaxMark = 10f
                    camLct = systemTimeInMillis()
                },
                markerFirstNames = "John",
                markerLastName = "Smith",
            )
        )
    )
}

