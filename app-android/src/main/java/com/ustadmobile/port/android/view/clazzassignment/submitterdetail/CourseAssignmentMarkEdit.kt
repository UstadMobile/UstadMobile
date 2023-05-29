package com.ustadmobile.port.android.view.clazzassignment.submitterdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.view.composable.UstadInputFieldLayout
import com.ustadmobile.port.android.view.composable.UstadNumberTextField

@Composable
fun CourseAssignmentMarkEdit(
    draftMark: CourseAssignmentMark,
    maxPoints: Float,
    scoreError: String?,
    onChangeDraftMark: (CourseAssignmentMark) -> Unit,
    onClickSubmitGrade: () -> Unit,
    onClickSubmitGradeAndMarkNext: () -> Unit,
    submitGradeButtonMessageId: Int,
    submitGradeButtonAndGoNextMessageId: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ){
        OutlinedTextField(
            modifier = Modifier
                .testTag("marker_comment")
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            label = { Text(stringResource(R.string.mark_comment)) },
            value = draftMark.camMarkerComment ?: "",
            onValueChange = {
                onChangeDraftMark(draftMark.shallowCopy {
                    camMarkerComment = it
                })
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            UstadInputFieldLayout(
                errorText = scoreError,
                modifier = Modifier.weight(1f)
            ) {
                UstadNumberTextField(
                    value = draftMark.camMark,
                    label = { Text(stringResource(R.string.mark)) },
                    modifier = Modifier
                        .testTag("marker_mark")
                        .padding(end = 8.dp),
                    isError = scoreError != null,
                    onValueChange = {
                        onChangeDraftMark(draftMark.shallowCopy {
                            camMark = it
                        })
                    },
                    trailingIcon = {
                        Text("/$maxPoints ${stringResource(R.string.points)}")
                    }
                )
            }

            Button(
                modifier = Modifier
                    .testTag("submit_mark_button")
                    .weight(1f)
                    .height(64.dp) //Height set to match the textfield - 48dp as per material design spec
                    .padding(top = 8.dp, start = 8.dp),
                onClick = onClickSubmitGrade,
            ) {
                Text(messageIdResource(submitGradeButtonMessageId))
            }
        }

        /*
        To be enabled when reactive sync is enabled
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("submit_and_mark_next"),
            onClick = onClickSubmitGradeAndMarkNext,
        ) {
            Text(stringResource(R.string.return_and_mark_next))
        }
         */
    }
}

@Composable
@Preview
private fun CourseAssignmentMarkEditPreview() {
    CourseAssignmentMarkEdit(
        draftMark = CourseAssignmentMark().apply {
            camMarkerComment = "Awful"
            camMark = 1f
        },
        maxPoints = 10f,
        scoreError = null,
        onChangeDraftMark = { },
        onClickSubmitGrade = { },
        submitGradeButtonMessageId = MessageID.submit,
        submitGradeButtonAndGoNextMessageId = MessageID.submit_grade_and_mark_next,
        onClickSubmitGradeAndMarkNext = { }
    )
}
