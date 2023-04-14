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

    val htmlStr = remember(uiState.courseBlock?.cbDescription) {
        Html.fromHtml(uiState.courseBlock?.cbDescription ?: "").toString()
    }

    Column(
        modifier = modifier
    ){

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("title")
                .defaultItemPadding(),
            value = uiState.courseBlock?.cbTitle?: "",
            label = { Text(stringResource(R.string.title)) },
            isError = uiState.caTitleError != null,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                    cbTitle = it
                })
            },
        )

        uiState.caTitleError?.also {
            Text(it)
        }

        UstadClickableTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("description")
                .defaultItemPadding(),
            label = { Text(stringResource(R.string.description)) },
            maxLines = 3,
            value = htmlStr,
            onClickEnabled = scrollState?.isScrollInProgress != true,
            readOnly = true,
            onClick = onClickEditDescription,
            onValueChange = {
                //do nothing
            }
        )

        UstadDateTimeEditTextField(
            value = uiState.courseBlock?.cbHideUntilDate ?: 0,
            modifier = Modifier
                .testTag("hide_until_date")
                .defaultItemPadding(),
            dateLabel = stringResource(id = R.string.dont_show_before)
                .addOptionalSuffix(),
            timeLabel = stringResource(id = R.string.time),
            timeZoneId = uiState.timeZone,
            error = uiState.caStartDateError
        )

        Text(
            text = stringResource(R.string.class_timezone_set, uiState.timeZone),
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row() {
            UstadMessageIdOptionExposedDropDownMenuField(
                modifier = Modifier.weight(0.5F).testTag("cbCompletionCriteria"),
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
                UstadNumberTextField(
                    modifier = Modifier
                        .weight(0.5F)
                        .testTag("cbMinPoints"),
                    value = (uiState.courseBlock?.cbMinPoints ?: 0).toFloat(),
                    label = { Text(stringResource(id = R.string.points)) },
                    enabled = uiState.fieldsEnabled,
                    trailingIcon = { Text(stringResource(id = R.string.points)) },
                    onValueChange = {
                        onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                            cbMinPoints = it.toInt()
                        })
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        UstadNumberTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cbMaxPoints"),
            value = (uiState.courseBlock?.cbMaxPoints ?: 0).toFloat(),
            label = { Text(stringResource(id = R.string.maximum_points)) },
            enabled = uiState.fieldsEnabled,
            isError = uiState.caMaxPointsError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
                onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                    cbMaxPoints = it.toInt()
                })
            },
        )

        uiState.caMaxPointsError?.also {
            Text(it)
        }

        Spacer(modifier = Modifier.height(10.dp))

        UstadDateTimeEditTextField(
            modifier = Modifier.testTag("cbDeadlineDate"),
            value = uiState.courseBlock?.cbDeadlineDate ?: 0,
            dateLabel = stringResource(id = R.string.deadline).addOptionalSuffix(),
            timeLabel = stringResource(id = R.string.time),
            error = uiState.caDeadlineError,
            timeZoneId = uiState.timeZone
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.gracePeriodVisible){
            UstadDateTimeEditTextField(
                modifier = Modifier.testTag("cbGracePeriodDate"),
                value = uiState.courseBlock?.cbGracePeriodDate ?: 0,
                dateLabel = stringResource(id = R.string.end_of_grace_period),
                timeLabel = stringResource(id = R.string.time),
                timeZoneId = uiState.timeZone
            )

            Spacer(modifier = Modifier.height(10.dp))

            UstadNumberTextField(
                modifier = Modifier.fillMaxWidth().testTag("cbLateSubmissionPenalty"),
                value = (uiState.courseBlock?.cbLateSubmissionPenalty ?: 0).toFloat(),
                label = { Text(stringResource(id = R.string.late_submission_penalty)) },
                keyboardOptions =  KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.caStartDateError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                        cbLateSubmissionPenalty = it.toInt()
                    })
                },
                trailingIcon = {
                    Text("%")
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
