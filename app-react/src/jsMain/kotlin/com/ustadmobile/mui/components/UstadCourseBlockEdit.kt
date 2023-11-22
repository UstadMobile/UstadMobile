package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.courseblock.CourseBlockViewModelConstants.CompletionCriteria
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.util.ext.addOptionalSuffix
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadSelectField
import com.ustadmobile.wrappers.quill.ReactQuill
import web.cssom.px
import js.core.jso
import kotlinx.datetime.TimeZone
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface UstadCourseBlockEditProps: Props {

    var uiState: CourseBlockEditUiState

    var onCourseBlockChange: ((CourseBlock?) -> Unit)

}

/**
 * Base component for showing detail fields e.g. phone number, start date, end date, etc.
 */
val UstadCourseBlockEdit = FC<UstadCourseBlockEditProps> { props ->

    val strings = useStringProvider()

    Stack{
        spacing = responsive(20.px)

        UstadTextField {
            id = "title"
            value = props.uiState.courseBlock?.cbTitle ?: ""
            label = ReactNode(strings[MR.strings.title])
            disabled = !props.uiState.fieldsEnabled
            fullWidth = true
            error = props.uiState.caTitleError != null
            helperText = props.uiState.caTitleError?.let { ReactNode(it) }
            onTextChange = {
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbTitle = it
                })
            }
        }


        ReactQuill {
            value = props.uiState.courseBlock?.cbDescription ?: ""
            id = "description_quill"
            placeholder = strings[MR.strings.description]
            onChange = {
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbDescription = it
                })
            }
        }


        UstadDateTimeField {
            timeInMillis = props.uiState.courseBlock?.cbHideUntilDate ?: 0
            label = ReactNode(strings[MR.strings.dont_show_before].addOptionalSuffix(strings))
            id = "hide_until_date"
            disabled = !props.uiState.fieldsEnabled
            helperText = props.uiState.caHideUntilDateError?.let { ReactNode(it) }
            error = props.uiState.caHideUntilDateError != null
            timeZoneId = props.uiState.timeZone
            onChange = {
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbHideUntilDate = it
                })
            }
        }

        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(15.px)


            if(props.uiState.completionCriteriaVisible) {
                UstadSelectField<CompletionCriteria> {
                    id = "cbCompletionCriteria"
                    value = CompletionCriteria.valueOf(props.uiState.courseBlock?.cbCompletionCriteria ?: 0)
                    label = strings[MR.strings.completion_criteria]
                    options = props.uiState.completionCriteriaOptions
                    itemValue = { it.value.toString() }
                    itemLabel = { ReactNode(strings[it.stringResource]) }
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                            cbCompletionCriteria = it.value
                        })
                    }
                }
            }

            if (props.uiState.minScoreVisible){
                UstadNumberTextField {
                    id = "cbMinPoints"
                    numValue = (props.uiState.courseBlock?.cbMinPoints ?: 0).toFloat()
                    asDynamic().InputProps = jso<InputBaseProps> {
                        endAdornment = InputAdornment.create {
                            position = InputAdornmentPosition.end
                            + strings[MR.strings.points]
                        }
                    }
                    label = ReactNode(strings[MR.strings.points])
                    disabled = !props.uiState.fieldsEnabled
                    onChange = {
                        props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                            cbMinPoints = it.toInt()
                        })
                    }
                }
            }
        }

        if(props.uiState.maxPointsVisible) {
            UstadNumberTextField {
                id = "cbMaxPoints"
                numValue = (props.uiState.courseBlock?.cbMaxPoints ?: 0).toFloat()
                label = ReactNode(strings[MR.strings.maximum_points])
                error = (props.uiState.caMaxPointsError != null)
                helperText = props.uiState.caMaxPointsError?.let { ReactNode(it) }
                disabled = !props.uiState.fieldsEnabled
                onChange = {
                    props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                        cbMaxPoints = it.toInt()
                    })
                }
            }
        }


        if(props.uiState.deadlineVisible) {
            UstadDateTimeField {
                id = "cbDeadlineDate"
                timeInMillis = props.uiState.courseBlock?.cbDeadlineDate ?: 0
                timeZoneId = props.uiState.timeZone
                unsetDefault = Long.MAX_VALUE
                label = ReactNode(strings[MR.strings.deadline].addOptionalSuffix(strings))
                disabled = !props.uiState.fieldsEnabled
                helperText = props.uiState.caDeadlineError?.let { ReactNode(it) }
                error = props.uiState.caDeadlineError != null
                onChange = {
                    props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                        cbDeadlineDate = it
                    })
                }
            }
        }


        if (props.uiState.gracePeriodVisible){
            UstadDateTimeField {
                id = "cbGracePeriodDate"
                timeInMillis = props.uiState.courseBlock?.cbGracePeriodDate ?: 0
                timeZoneId = props.uiState.timeZone
                unsetDefault = Long.MAX_VALUE
                label = ReactNode(strings[MR.strings.end_of_grace_period])
                disabled = !props.uiState.fieldsEnabled
                helperText = props.uiState.caGracePeriodError?.let { ReactNode(it) }
                error = props.uiState.caGracePeriodError != null
                timeZoneId = TimeZone.currentSystemDefault().id
                onChange = {
                    props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                        cbGracePeriodDate = it
                    })
                }
            }
        }

        if(props.uiState.latePenaltyVisible) {
            UstadNumberTextField {
                id = "cbLateSubmissionPenalty"
                numValue = (props.uiState.courseBlock?.cbLateSubmissionPenalty ?: 0).toFloat()
                label = ReactNode(strings[MR.strings.late_submission_penalty])
                disabled = !props.uiState.fieldsEnabled
                helperText = ReactNode(strings[MR.strings.penalty_label])
                asDynamic().InputProps = jso<InputBaseProps> {
                    endAdornment = InputAdornment.create {
                        position = InputAdornmentPosition.end
                        + "%"
                    }
                }
                onChange = { newString ->
                    props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                        cbLateSubmissionPenalty = newString.toInt()
                    })
                }
            }
        }
    }
}

val UstadCourseBlockEditPreview = FC<Props> {
    Container {
        maxWidth = "lg"

        UstadCourseBlockEdit {
            uiState = CourseBlockEditUiState(
                courseBlock = CourseBlock().apply {
                    cbMaxPoints = 78
                    cbCompletionCriteria = ContentEntry.COMPLETION_CRITERIA_MIN_SCORE
                },
            )
        }
    }
}