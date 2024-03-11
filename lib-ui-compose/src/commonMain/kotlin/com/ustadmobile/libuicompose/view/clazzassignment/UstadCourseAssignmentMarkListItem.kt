package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.ext.penaltyPercentage
import com.ustadmobile.core.util.ext.roundTo
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.util.rememberDayOrDate
import java.text.DateFormat

@Composable
fun UstadCourseAssignmentMarkListItem(
    uiState: UstadCourseAssignmentMarkListItemUiState,
    timeFormatter: DateFormat,
    dateFormat: DateFormat,
    modifier: Modifier = Modifier,
){

    var text = uiState.markerName

    if (uiState.markerGroupNameVisible){
        text += "  (${stringResource(MR.strings.group_number, uiState.peerGroupNumber)})"
    }

    val dayOrDate = rememberDayOrDate(
        localDateTimeNow = uiState.localDateTimeNow,
        timestamp = uiState.mark.courseAssignmentMark?.camLct ?: 0,
        timeZone = kotlinx.datetime.TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = timeFormatter,
        dateFormatter = dateFormat,
        dayOfWeekStringMap = uiState.dayOfWeekStrings,
    )

    ListItem(
        modifier = modifier,
        leadingContent = {
            UstadPersonAvatar(
                personName = uiState.markerName,
                pictureUri = uiState.mark.markerPictureUri,
            )
        },
        headlineContent = { Text(text) },
        supportingContent = {
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = "",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        buildAnnotatedString {
                            append("${uiState.mark.courseAssignmentMark?.camMark?.roundTo(2)}")
                            append("/${uiState.mark.courseAssignmentMark?.camMaxMark?.roundTo(2)}")
                            append(" ${stringResource(MR.strings.points)}")

                            if (uiState.camPenaltyVisible){
                                withStyle(
                                    style = SpanStyle(color = MaterialTheme.colorScheme.error)
                                ) {
                                    append(" ")
                                    append(
                                        stringResource(
                                            MR.strings.late_penalty,
                                            (uiState.mark.courseAssignmentMark?.penaltyPercentage() ?: 0).toString() + "%"
                                        )
                                    )
                                }
                            }
                        }
                    )
                }

                if(uiState.mark.courseAssignmentMark?.camMarkerComment?.isBlank() == false) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Filled.Chat,
                            contentDescription = "",
                            modifier = Modifier.size(16.dp)
                        )

                        Text(uiState.mark.courseAssignmentMark?.camMarkerComment ?: "")
                    }
                }
            }
        },
        trailingContent = {
            Text(dayOrDate)
        }
    )
}

