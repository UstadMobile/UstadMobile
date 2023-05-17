package com.ustadmobile.port.android.view.clazzassignment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
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
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.lib.db.entities.CourseAssignmentMarkWithPersonMarker
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.util.compose.rememberFormattedTime

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadCourseAssignmentMarkListItem(
    uiState: UstadCourseAssignmentMarkListItemUiState,
    modifier: Modifier = Modifier,
    onClickMark: (CourseAssignmentMarkWithPersonMarker?) -> Unit = {},
){

    var text = uiState.mark.marker?.fullName() ?: ""

    if (uiState.markerGroupNameVisible){
        text += "  (${stringResource(R.string.group_number, uiState.mark.camMarkerSubmitterUid)})"
    }

    val formattedTime = rememberFormattedTime(uiState.mark.camLct.toInt())

    ListItem(
        modifier = modifier.clickable {
            onClickMark(uiState.mark)
        },
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
                            append("${uiState.mark.camMark}/${uiState.block.cbMaxPoints}" +
                                    " ${stringResource(R.string.points)}")


                            if (uiState.camPenaltyVisible){
                                withStyle(style = SpanStyle(color = colorResource(R.color.errorColor)))
                                {
                                    append("  "+stringResource(R.string.late_penalty,
                                        uiState.block.cbLateSubmissionPenalty))
                                }
                            }
                        }
                    )
                }
                Text(uiState.mark.camMarkerComment ?: "")
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
            mark = CourseAssignmentMarkWithPersonMarker().apply {
                marker = Person().apply {
                    firstNames = "John"
                    lastName = "Smith"
                    isGroup = true
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                    camPenalty = 3
                }
            }
        )
    )
}

