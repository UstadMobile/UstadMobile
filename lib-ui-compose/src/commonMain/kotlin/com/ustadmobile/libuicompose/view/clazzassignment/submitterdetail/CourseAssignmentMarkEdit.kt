package com.ustadmobile.libuicompose.view.clazzassignment.submitterdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import dev.icerock.moko.resources.StringResource
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadNumberTextField
import dev.icerock.moko.resources.compose.stringResource
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
                .testTag("mark_comment")
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
            UstadNumberTextField(
                value = draftMark.camMark,
                label = { Text(mrStringResource(MR.strings.mark) + "*") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("mark")
                    .padding(end = 8.dp),
                isError = scoreError != null,
                onValueChange = {
                    onChangeDraftMark(draftMark.shallowCopy {
                        camMark = it
                    })
                },
                unsetDefault = -1f,
                trailingIcon = {
                    Text(
                        modifier = Modifier.padding(end = 8.dp),
                        text = "/$maxPoints"
                    )
                },
                supportingText = {
                    Text(scoreError ?: stringResource(MR.strings.required))
                }
            )

            OutlinedButton(
                modifier = Modifier
                    .testTag("submit_grade")
                    .weight(1f)
                    .height(64.dp) //Height set to match the textfield - 48dp as per material design spec
                    .padding(top = 8.dp, start = 8.dp),
                onClick = onClickSubmitGrade,
            ) {
                Text(mrStringResource(submitGradeButtonMessageId))
            }
        }

    }
}
