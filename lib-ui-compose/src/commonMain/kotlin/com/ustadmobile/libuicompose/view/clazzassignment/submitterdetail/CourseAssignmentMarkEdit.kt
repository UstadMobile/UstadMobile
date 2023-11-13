package com.ustadmobile.libuicompose.view.clazzassignment.submitterdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import dev.icerock.moko.resources.StringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadNumberTextField
import dev.icerock.moko.resources.compose.stringResource as mrStringResource

@Composable
fun CourseAssignmentMarkEdit(
    draftMark: CourseAssignmentMark,
    maxPoints: Float,
    scoreError: String?,
    onChangeDraftMark: (CourseAssignmentMark) -> Unit,
    onClickSubmitGrade: () -> Unit,
    onClickSubmitGradeAndMarkNext: () -> Unit,
    submitGradeButtonMessageId: StringResource,
    submitGradeButtonAndGoNextMessageId: StringResource,
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
            label = { Text(mrStringResource(MR.strings.mark_comment)) },
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
                    label = { Text(mrStringResource(MR.strings.mark)) },
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
                        Text(
                            modifier = Modifier.padding(end = 8.dp),
                            text = "/$maxPoints"
                        )
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
                Text(mrStringResource(submitGradeButtonMessageId))
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
            Text(stringResource(CR.string.return_and_mark_next))
        }
         */
    }
}

//@Composable
//@Preview
//private fun CourseAssignmentMarkEditPreview() {
//    CourseAssignmentMarkEdit(
//        draftMark = CourseAssignmentMark().apply {
//            camMarkerComment = "Awful"
//            camMark = 1f
//        },
//        maxPoints = 10f,
//        scoreError = null,
//        onChangeDraftMark = { },
//        onClickSubmitGrade = { },
//        submitGradeButtonMessageId = MR.strings.submit,
//        submitGradeButtonAndGoNextMessageId = MR.strings.submit_grade_and_mark_next,
//        onClickSubmitGradeAndMarkNext = { }
//    )
//}
