package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.courseblock.CourseBlockViewModelConstants.CompletionCriteria
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.UNSET_DISTANT_FUTURE
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun UstadCourseBlockEdit(
    uiState: CourseBlockEditUiState,
    modifier: Modifier = Modifier,
    onCourseBlockChange: (CourseBlock?) -> Unit = {},
    onClickEditDescription: () -> Unit = {},
    onClickEditContentEntry: () -> Unit = { },
){
    Column(
        modifier = modifier
    ) {
        uiState.selectedContentEntry?.also { selectedContentEntry ->
            ListItem(
                headlineContent = {
                    Text(selectedContentEntry.entry?.title ?: "")
                },
                supportingContent = {
                    Text(stringResource(MR.strings.selected_content))
                },
                leadingContent = {
                    Icon(Icons.Outlined.Book, contentDescription = null)
                },
                trailingContent = if(uiState.canEditSelectedContentEntry) {
                    {
                        UstadTooltipBox(
                            tooltipText = stringResource(MR.strings.edit)
                        ) {
                            IconButton(
                                onClick = onClickEditContentEntry
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = stringResource(MR.strings.edit))
                            }
                        }
                    }
                }else {
                    null
                }
            )
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("title")
                .defaultItemPadding(),
            value = uiState.courseBlock?.cbTitle ?: "",
            label = { Text(stringResource(MR.strings.title) + "*") },
            isError = uiState.caTitleError != null,
            enabled = uiState.fieldsEnabled,
            singleLine = true,
            supportingText = {
                Text(uiState.caTitleError ?: stringResource(MR.strings.required))
            },
            onValueChange = {
                onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                    cbTitle = it
                })
            },
        )

        UstadRichTextEdit(
            html = uiState.courseBlock?.cbDescription ?: "",
            modifier = Modifier.fillMaxWidth().defaultItemPadding()
                .testTag("description"),
            onHtmlChange = {
                uiState.courseBlock?.also { courseBlock ->
                    onCourseBlockChange(courseBlock.shallowCopy {
                        cbDescription = it
                    })
                }
            },
            onClickToEditInNewScreen = onClickEditDescription,
            editInNewScreenLabel = stringResource(MR.strings.description),
            placeholderText = stringResource(MR.strings.description),
        )

        UstadInputFieldLayout(
            modifier = Modifier.defaultItemPadding(),
            errorText = uiState.caHideUntilDateError,
        ) {
            UstadDateTimeField(
                value = uiState.courseBlock?.cbHideUntilDate ?: 0,
                modifier = Modifier
                    .testTag("dont_show_before"),
                dateLabel = { Text(stringResource(MR.strings.dont_show_before)) },
                timeLabel = { Text(stringResource(MR.strings.time)) },
                timeZoneId = uiState.timeZone,
                onValueChange = {
                    onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                        cbHideUntilDate = it
                    })
                },
                baseTestTag = "dont_show_before",
            )
        }

        Row {
            if(uiState.completionCriteriaVisible) {
                UstadExposedDropDownMenuField<CompletionCriteria>(
                    modifier = Modifier
                        .testTag("completion_criteria")
                        .fillMaxWidth()
                        .weight(0.5F)
                        .defaultItemPadding(end = if (uiState.minScoreVisible) 8.dp else 16.dp),
                    value = CompletionCriteria.valueOf(
                        uiState.courseBlock?.cbCompletionCriteria ?: 0
                    ),
                    label = stringResource(MR.strings.completion_criteria),
                    itemText = { stringResource(it.stringResource).capitalizeFirstLetter() },
                    options = uiState.completionCriteriaOptions,
                    onOptionSelected = {
                        onCourseBlockChange(uiState.courseBlock?.shallowCopy{
                            cbCompletionCriteria = it.value
                        })
                    },
                    enabled = uiState.fieldsEnabled,
                )
            }

            if (uiState.minScoreVisible) {
                Spacer(modifier = Modifier.width(15.dp))

                UstadNumberTextField(
                    modifier = Modifier
                        .testTag("points")
                        .weight(0.5F)
                        .defaultItemPadding(start = 0.dp),
                    value = (uiState.courseBlock?.cbMinPoints?.toFloat() ?: 0f),
                    label = { Text(stringResource(MR.strings.points)) },
                    enabled = uiState.fieldsEnabled,
                    trailingIcon = {
                        Text(
                            text = stringResource(MR.strings.points),
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    onValueChange = {
                        onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                            cbMinPoints = it.toInt()
                        })
                    },
                )
            }
        }


        if(uiState.maxPointsVisible) {
            UstadInputFieldLayout(
                modifier = Modifier.defaultItemPadding(),
                errorText = uiState.caMaxPointsError,
            ) {
                UstadNumberTextField(
                    modifier = Modifier
                        .testTag("maximum_points")
                        .fillMaxWidth(),
                    value = (uiState.courseBlock?.cbMaxPoints?.toFloat() ?: 0f),
                    label = { Text(stringResource(MR.strings.maximum_points)) },
                    trailingIcon = {
                        Text(
                            text = stringResource(MR.strings.points),
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    isError = uiState.caMaxPointsError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                            cbMaxPoints = it.toInt()
                        })
                    },
                )
            }
        }


        if(uiState.deadlineVisible) {
            UstadInputFieldLayout(
                modifier = Modifier.defaultItemPadding(),
                errorText = uiState.caDeadlineError
            ) {
                UstadDateTimeField(
                    modifier = Modifier.testTag("deadline"),
                    value = uiState.courseBlock?.cbDeadlineDate ?: 0,
                    isError = uiState.caDeadlineError != null,
                    unsetDefault = UNSET_DISTANT_FUTURE,
                    dateLabel = { Text(stringResource(MR.strings.deadline)) },
                    timeLabel = { stringResource(MR.strings.time) },
                    timeZoneId = uiState.timeZone,
                    onValueChange = {
                        onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                            cbDeadlineDate = it
                        })
                    },
                    baseTestTag = "deadline"
                )
            }
        }


        if (uiState.gracePeriodVisible){
            UstadInputFieldLayout(
                modifier = Modifier.defaultItemPadding(),
                errorText = uiState.caGracePeriodError,
            ) {
                UstadDateTimeField(
                    modifier = Modifier.testTag("end_of_grace_period"),
                    value = uiState.courseBlock?.cbGracePeriodDate ?: 0,
                    unsetDefault = UNSET_DISTANT_FUTURE,
                    dateLabel = { Text(stringResource(MR.strings.end_of_grace_period)) },
                    timeLabel = { stringResource(MR.strings.time) },
                    timeZoneId = uiState.timeZone,
                    baseTestTag = "end_of_grace_period",
                    onValueChange = {
                        onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                            cbGracePeriodDate = it
                        })
                    }
                )
            }
        }

        if(uiState.latePenaltyVisible) {
            UstadNumberTextField(
                modifier = Modifier
                    .defaultItemPadding(bottom = 0.dp)
                    .fillMaxWidth()
                    .testTag("late_submission_penalty"),
                value = (uiState.courseBlock?.cbLateSubmissionPenalty?.toFloat() ?: 0f),
                label = { Text(stringResource(MR.strings.late_submission_penalty)) },
                enabled = uiState.fieldsEnabled,
                trailingIcon = {
                    Text(text = "%", modifier = Modifier.padding(end = 16.dp))
                },
                keyboardOptions =  KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = {
                    onCourseBlockChange(uiState.courseBlock?.shallowCopy {
                        cbLateSubmissionPenalty = it.toInt()
                    })
                },
                supportingText = {
                    Text(stringResource(MR.strings.penalty_label))
                }
            )
        }
    }
}
