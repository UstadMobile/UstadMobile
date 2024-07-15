package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.AverageCourseAssignmentMark
import com.ustadmobile.libuicompose.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewConstants.ASSIGNMENT_STATUS_MAP
import dev.icerock.moko.resources.compose.stringResource


fun LazyListScope.UstadAssignmentSubmissionStatusHeaderItems(
    submissionStatus: Int?,
    averageMark: AverageCourseAssignmentMark?,
    maxPoints: Float = 0f,
    submissionPenaltyPercent: Int = 0,
) {
    item(key = "submission_status") {
        if(submissionStatus != null) {
            val icon = ASSIGNMENT_STATUS_MAP[submissionStatus] ?: Icons.Default.Done
            ListItem(
                leadingContent = {
                    Icon(
                        icon,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(
                        stringResource(
                            SubmissionConstants.STATUS_MAP[submissionStatus]
                                ?: MR.strings.not_submitted_cap
                        )
                    )
                },
                supportingContent = { Text(stringResource(MR.strings.status)) }
            )
        }
    }

    item {
        if(averageMark != null) {
            val pointsString : AnnotatedString = buildAnnotatedString {
                append("${averageMark.averageScore}/$maxPoints ${stringResource(MR.strings.points)}")

                if (averageMark.averagePenalty != 0){
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append(" ")
                        append(
                            stringResource(MR.strings.late_penalty, "$submissionPenaltyPercent%")
                        )
                    }
                }
            }

            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null
                    )
                },
                headlineContent = { Text(pointsString) },
                supportingContent = {
                    Text(stringResource(MR.strings.xapi_result_header))
                }
            )
        }
    }

}

