package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.tooling.preview.Preview
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.viewmodel.UstadAssignmentFileSubmissionHeaderUiState
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.view.ClazzAssignmentDetailOverviewFragment


@Composable
fun UstadAssignmentFileSubmissionHeader(
    uiState: UstadAssignmentFileSubmissionHeaderUiState,
    modifier: Modifier = Modifier,
){
    Column(
        modifier = modifier
    ) {


        var imageId = 0
        if (uiState.submissionStatusIconVisible) {
            imageId = (ClazzAssignmentDetailOverviewFragment.ASSIGNMENT_STATUS_MAP
                    [uiState.assignmentStatus] ?: R.drawable.ic_done_white_24dp)
        }


        UstadDetailField(
            modifier = Modifier.defaultItemPadding(),
            valueText = messageIdResource(SubmissionConstants.STATUS_MAP
                    [uiState.assignmentStatus] ?: MessageID.not_submitted_cap),
            labelText = stringResource(R.string.status),
            imageId = imageId,
        )


        if (uiState.showPoints){

            val pointsString : AnnotatedString = buildAnnotatedString {
                append("${uiState.assignmentMark.averageScore}" +
                        "/${uiState.block.cbMaxPoints}" +
                        stringResource(R.string.points)
                )


                if (uiState.latePenaltyVisible){
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append(" "+stringResource(R.string.late_penalty,
                            uiState.block.cbLateSubmissionPenalty))
                    }
                }
            }

            UstadDetailField(
                modifier = Modifier.defaultItemPadding(),
                valueText = pointsString,
                labelText = buildAnnotatedString {
                    append(stringResource(R.string.xapi_result_header))
                },
                imageId = R.drawable.ic_baseline_emoji_events_24,
            )
        }

    }
}


@Composable
@Preview
private fun UstadAssignmentFileSubmissionHeaderPreview() {
    UstadAssignmentFileSubmissionHeader(
        uiState = UstadAssignmentFileSubmissionHeaderUiState(
            assignmentStatus = CourseAssignmentSubmission.NOT_SUBMITTED
        )
    )
}