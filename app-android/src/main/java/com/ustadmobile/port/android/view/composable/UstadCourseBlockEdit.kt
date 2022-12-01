package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.locale.entityconstants.CompletionCriteriaConstants
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy

@Composable
fun CourseBlockEdit(
    uiState: CourseBlockEditUiState,
    onCourseBlockChange: (CourseBlock?) -> Unit = {}
){

    Column{

        Row {
            Column(
                modifier = Modifier.weight(0.5F)
            ) {

                UstadDateEditTextField(
                    value = uiState.courseBlock?.cbHideUntilDate ?: 0,
                    label = stringResource(id = R.string.dont_show_before)
                        .addOptionalSuffix(),
                    error = uiState.caStartDateError,
                    enabled = uiState.fieldsEnabled,
                    onValueChange = {
                        onCourseBlockChange(uiState.courseBlock?.shallowCopy{
                            cbHideUntilDate = it
                        })
                    }
                )

                Text(stringResource(R.string.class_timezone_set, uiState.timeZone))
            }

            Spacer(modifier = Modifier.width(15.dp))

            UstadDateEditTextField(
                modifier = Modifier.weight(0.5F),
                value = uiState.courseBlock?.cbHideUntilDate ?: 0,
                label = stringResource(id = R.string.time),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onCourseBlockChange(uiState.courseBlock?.shallowCopy{
                        cbHideUntilDate = it
                    })
                }
            )
        }

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
                    value = ((uiState.courseBlock?.cbMaxPoints ?: 0).toString() + " " +
                            stringResource(id = R.string.points)),
                    label = stringResource(id = R.string.maximum_points),
                    enabled = uiState.fieldsEnabled,
                    onValueChange = {
                        onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                            cbMaxPoints = it.toInt()
                        })
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        UstadTextEditField(
            value = ((uiState.courseBlock?.cbMaxPoints ?: 0).toString() + " " +
                    stringResource(id = R.string.points)),
            label = stringResource(id = R.string.points),
            error = uiState.caStartDateError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                    cbMaxPoints = it.toInt()
                })
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row {
            UstadDateEditTextField(
                modifier = Modifier.weight(0.5F),
                value = uiState.courseBlock?.cbDeadlineDate ?: 0,
                label = stringResource(id = R.string.deadline).addOptionalSuffix(),
                error = uiState.caDeadlineError,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onCourseBlockChange(uiState.courseBlock?.shallowCopy{
                        cbDeadlineDate = it
                    })
                }
            )

            Spacer(modifier = Modifier.width(15.dp))

            UstadDateEditTextField(
                modifier = Modifier.weight(0.5F),
                value = uiState.courseBlock?.cbDeadlineDate ?: 0,
                label = stringResource(id = R.string.time),
                error = uiState.caDeadlineError,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onCourseBlockChange(uiState.courseBlock?.shallowCopy{
                        cbDeadlineDate = it
                    })
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.gracePeriodVisible){

            Row {
                UstadDateEditTextField(
                    modifier = Modifier.weight(0.5F),
                    value = uiState.courseBlock?.cbGracePeriodDate ?: 0,
                    label = stringResource(id = R.string.end_of_grace_period),
                    error = uiState.caGracePeriodError,
                    enabled = uiState.fieldsEnabled,
                    onValueChange = {
                        onCourseBlockChange(uiState.courseBlock?.shallowCopy{
                            cbGracePeriodDate = it
                        })
                    }
                )

                Spacer(modifier = Modifier.width(15.dp))

                UstadDateEditTextField(
                    modifier = Modifier.weight(0.5F),
                    value = uiState.courseBlock?.cbGracePeriodDate ?: 0,
                    label = stringResource(id = R.string.time),
                    error = uiState.caGracePeriodError,
                    enabled = uiState.fieldsEnabled,
                    onValueChange = {
                        onCourseBlockChange(uiState.courseBlock?.shallowCopy{
                            cbGracePeriodDate = it
                        })
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            UstadTextEditField(
                value = (uiState.courseBlock?.cbLateSubmissionPenalty ?: 0)
                    .toString()+" %",
                label = stringResource(id = R.string.late_submission_penalty),
                error = uiState.caStartDateError,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                        cbLateSubmissionPenalty = it.toInt()
                    })
                }
            )

            Text(text = stringResource(id = R.string.penalty_label))
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
        },
        minScoreVisible = true,
        gracePeriodVisible = true,
    )
    CourseBlockEdit(uiState)
}
