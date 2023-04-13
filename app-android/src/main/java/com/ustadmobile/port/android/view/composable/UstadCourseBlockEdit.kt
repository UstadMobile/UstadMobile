package com.ustadmobile.port.android.view.composable

import android.text.Html
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.locale.entityconstants.CompletionCriteriaConstants
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.ext.defaultItemPadding

@Composable
fun UstadCourseBlockEdit(
    uiState: CourseBlockEditUiState,
    modifier: Modifier = Modifier,
    onCourseBlockChange: (CourseBlock?) -> Unit = {},
    onClickEditDescription: () -> Unit = {},
    scrollState: ScrollState? = null,
){

    Column(
        modifier = modifier
    ){
        
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("title")
                .defaultItemPadding(),
            value = uiState.courseBlock?.cbTitle?: "",
            onValueChange = {
                onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                    cbTitle = it
                })
            },
            label = { Text(stringResource(R.string.title)) }
        )

        HtmlClickableTextField(
            html = uiState.courseBlock?.cbDescription ?: "",
            label = stringResource(R.string.description),
            onClick = onClickEditDescription,
            modifier = Modifier.fillMaxWidth().testTag("description")
        )

        UstadDateTimeEditTextField(
            value = uiState.courseBlock?.cbHideUntilDate ?: 0,
            modifier = Modifier
                .testTag("hide_until_date")
                .defaultItemPadding(),
            dateLabel = stringResource(id = R.string.dont_show_before)
                .addOptionalSuffix(),
            timeLabel = stringResource(id = R.string.time),
            timeZoneId = uiState.timeZone
        )

        Text(
            text = stringResource(R.string.class_timezone_set, uiState.timeZone),
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row() {
            UstadMessageIdOptionExposedDropDownMenuField(
                modifier = Modifier.weight(0.5F),
                value = uiState.courseBlock?.cbCompletionCriteria ?: 0,
                label = stringResource(R.string.completion_criteria),
                options = CompletionCriteriaConstants.COMPLETION_CRITERIA_MESSAGE_IDS,
                onOptionSelected = {
                    onCourseBlockChange(uiState.courseBlock?.shallowCopy{
                        cbCompletionCriteria = it.value
                    })
                },
                enabled = uiState.fieldsEnabled,
            )

            Spacer(modifier = Modifier.width(15.dp))

            if (uiState.minScoreVisible){
                UstadTextEditField(
                    modifier = Modifier.weight(0.5F),
                    value = (uiState.courseBlock?.cbMaxPoints ?: 0).toString(),
                    suffixText = stringResource(id = R.string.points),
                    label = stringResource(id = R.string.points),
                    enabled = uiState.fieldsEnabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { newString ->
                        onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                            cbMaxPoints = newString.filter { it.isDigit() }.toIntOrNull() ?: 0
                        })
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        UstadTextEditField(
            value = (uiState.courseBlock?.cbMaxPoints ?: 0).toString(),
            suffixText =  stringResource(id = R.string.points),
            label = stringResource(id = R.string.maximum_points),
            error = uiState.caStartDateError,
            enabled = uiState.fieldsEnabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = { newString ->
                onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                    cbMaxPoints = newString.filter { it.isDigit() }.toIntOrNull() ?: 0
                })
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        UstadDateTimeEditTextField(
            value = uiState.courseBlock?.cbDeadlineDate ?: 0,
            dateLabel = stringResource(id = R.string.deadline).addOptionalSuffix(),
            timeLabel = stringResource(id = R.string.time),
            timeZoneId = uiState.timeZone
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.gracePeriodVisible){
            UstadDateTimeEditTextField(
                value = uiState.courseBlock?.cbGracePeriodDate ?: 0,
                dateLabel = stringResource(id = R.string.end_of_grace_period),
                timeLabel = stringResource(id = R.string.time),
                timeZoneId = uiState.timeZone
            )

            Spacer(modifier = Modifier.height(10.dp))

            UstadTextEditField(
                value = (uiState.courseBlock?.cbLateSubmissionPenalty ?: 0).toString(),
                label = stringResource(id = R.string.late_submission_penalty),
                error = uiState.caStartDateError,
                enabled = uiState.fieldsEnabled,
                suffixText = "%",
                keyboardOptions =  KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = { newString ->
                    onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                        cbLateSubmissionPenalty = newString.filter { it.isDigit() }.toIntOrNull() ?: 0
                    })
                }
            )

            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = stringResource(id = R.string.penalty_label)
            )
        }
    }

}

@Composable
@Preview
private fun CourseBlockEditPreview() {
    val uiState = CourseBlockEditUiState(
        courseBlock = CourseBlock().apply {
            cbMaxPoints = 78
            cbCompletionCriteria = 14
            cbCompletionCriteria = ContentEntry.COMPLETION_CRITERIA_MIN_SCORE
        },
        gracePeriodVisible = true,
    )
    UstadCourseBlockEdit(uiState)
}
