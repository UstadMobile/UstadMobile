package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.viewmodel.clazzassignment.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewConstants.ASSIGNMENT_STATUS_MAP
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadAssignmentSubmissionHeader(
    uiState: UstadAssignmentSubmissionHeaderUiState,
    modifier: Modifier = Modifier,
){
    Column(
        modifier = modifier
    ) {


        var icon = Icons.Default.Done
        if (uiState.submissionStatusIconVisible) {
            icon = (ASSIGNMENT_STATUS_MAP
                    [uiState.assignmentStatus] ?: Icons.Default.Done)
        }


        ListItem(
            modifier = Modifier
                .defaultItemPadding(),
            icon = {
                Icon(
                    icon,
                    contentDescription = null
                )
            },
            text = { Text(stringResource(SubmissionConstants.STATUS_MAP
                [uiState.assignmentStatus] ?: MR.strings.not_submitted_cap)) },
            secondaryText = { Text(stringResource(MR.strings.status)) }
        )


        if (uiState.showPoints){

            val pointsString : AnnotatedString = buildAnnotatedString {
                append((uiState.assignmentMark?.averageScore?.toString() ?: "") +
                        "/${uiState.block?.cbMaxPoints ?: 0}" +
                        stringResource(MR.strings.points)
                )


                if (uiState.latePenaltyVisible){
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append(" "+stringResource(MR.strings.late_penalty,
                            uiState.block?.cbLateSubmissionPenalty ?: 0))
                    }
                }
            }

            ListItem(
                modifier = Modifier
                    .defaultItemPadding(),
                icon = {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null
                    )
                },
                text = { Text(pointsString) },
                secondaryText = {
                    buildAnnotatedString {
                        append(stringResource(MR.strings.xapi_result_header))
                    }
                }
            )
        }

    }
}