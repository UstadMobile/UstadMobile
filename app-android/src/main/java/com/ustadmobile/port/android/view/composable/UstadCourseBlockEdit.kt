package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
            modifier = Modifier
                .fillMaxWidth()
                .testTag("description")
        )

        UstadDateTimeField(
            value = uiState.courseBlock?.cbHideUntilDate ?: 0,
            modifier = Modifier
                .testTag("hide_until_date")
                .defaultItemPadding(),
            dateLabel = { Text(stringResource(R.string.dont_show_before).addOptionalSuffix()) },
            timeLabel = { Text(stringResource(R.string.time)) },
            timeZoneId = uiState.timeZone,
            onValueChange = {
                onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                    cbHideUntilDate = it
                })
            }
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
                UstadNumberTextField(
                    modifier = Modifier.weight(0.5F),
                    value = (uiState.courseBlock?.cbMaxPoints?.toFloat() ?: 0f),
                    label = { Text(stringResource(R.string.points)) },
                    enabled = uiState.fieldsEnabled,
                    trailingIcon = {
                        Text(
                            text = stringResource(R.string.points),
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    onValueChange = {
                        onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                            cbMaxPoints = it.toInt()
                        })
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        UstadNumberTextField(
            modifier = Modifier.defaultItemPadding().fillMaxWidth(),
            value = (uiState.courseBlock?.cbMaxPoints?.toFloat() ?: 0f),
            trailingIcon = {
                Text(
                    text = stringResource(R.string.points),
                    modifier = Modifier.padding(end = 16.dp)
                )
            },
            label = { Text(stringResource(id = R.string.maximum_points)) },
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                    cbMaxPoints = it.toInt()
                })
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        UstadDateTimeField(
            value = uiState.courseBlock?.cbDeadlineDate ?: 0,
            unsetDefault = Long.MAX_VALUE,
            dateLabel = { Text(stringResource(id = R.string.deadline).addOptionalSuffix()) },
            timeLabel = { stringResource(id = R.string.time) },
            timeZoneId = uiState.timeZone,
            onValueChange = {
                onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                    cbDeadlineDate = it
                })
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.gracePeriodVisible){
            UstadDateTimeField(
                value = uiState.courseBlock?.cbGracePeriodDate ?: 0,
                unsetDefault = Long.MAX_VALUE,
                dateLabel = { Text(stringResource(id = R.string.end_of_grace_period)) },
                timeLabel = { stringResource(id = R.string.time) },
                timeZoneId = uiState.timeZone,
                onValueChange = {
                    onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                        cbGracePeriodDate = it
                    })
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            UstadNumberTextField(
                modifier = Modifier.fillMaxWidth().testTag("late_penalty_field"),
                value = (uiState.courseBlock?.cbLateSubmissionPenalty?.toFloat() ?: 0f),
                label = { Text(stringResource(id = R.string.late_submission_penalty)) },
                //error = uiState.caStartDateError,
                enabled = uiState.fieldsEnabled,
                trailingIcon = {
                    Text(text = "%", modifier = Modifier.padding(end = 16.dp))
                },
                onValueChange = {
                    onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                        cbLateSubmissionPenalty = it.toInt()
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
