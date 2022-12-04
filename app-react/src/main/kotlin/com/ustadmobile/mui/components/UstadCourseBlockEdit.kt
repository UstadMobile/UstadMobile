package com.ustadmobile.mui.components

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.CompletionCriteriaConstants
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.util.ext.addOptionalSuffix
import com.ustadmobile.util.ext.format
import csstype.px
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props

external interface UstadCourseBlockEditProps: Props {

    var uiState: CourseBlockEditUiState

    var onCourseBlockChange: ((CourseBlock?) -> Unit)

}

/**
 * Base component for showing detail fields e.g. phone number, start date, end date, etc.
 */
val UstadCourseBlockEdit = FC<UstadCourseBlockEditProps> { props ->

    val strings = useStringsXml()

    Stack{
        spacing = responsive(20.px)

        UstadDateTimeEditField {
            timeInMillis = props.uiState.courseBlock?.cbHideUntilDate ?: 0
            label = strings[MessageID.dont_show_before].addOptionalSuffix(strings)
            enabled = props.uiState.fieldsEnabled
            onChange = {
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbHideUntilDate = it
                })
            }
        }

        + strings[MessageID.class_timezone_set].format(props.uiState.timeZone)

        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(15.px)

            UstadMessageIdDropDownField {
                value = props.uiState.courseBlock?.cbCompletionCriteria ?: 0
                label = strings[MessageID.completion_criteria]
                options = CompletionCriteriaConstants.COMPLETION_CRITERIA_MESSAGE_IDS
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                        cbCompletionCriteria = it?.value ?: 0
                    })
                }
            }

            if (props.uiState.minScoreVisible){
                UstadTextEditField {
                    value = ((props.uiState.courseBlock?.cbMaxPoints ?: 0).toString() + " " +
                            strings[MessageID.points])
                    label = strings[MessageID.maximum_points]
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                            cbMaxPoints = it.toInt()
                        })
                    }
                }
            }
        }

        UstadTextEditField {
            value = ((props.uiState.courseBlock?.cbMaxPoints ?: 0).toString() + " " +
                    strings[MessageID.points])
            label = strings[MessageID.points]
            error = props.uiState.caStartDateError
            enabled = props.uiState.fieldsEnabled
            onChange = { newString ->
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbMaxPoints = newString.filter { it.isDigit() }.toIntOrNull() ?: 0
                })
            }
        }

        UstadDateTimeEditField {
            timeInMillis = props.uiState.courseBlock?.cbDeadlineDate ?: 0
            label = strings[MessageID.deadline].addOptionalSuffix(strings)
            enabled = props.uiState.fieldsEnabled
            onChange = {
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbDeadlineDate = it
                })
            }
        }

        if (props.uiState.gracePeriodVisible){

            UstadDateTimeEditField {
                timeInMillis = props.uiState.courseBlock?.cbGracePeriodDate ?: 0
                label = strings[MessageID.end_of_grace_period]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                        cbGracePeriodDate = it
                    })
                }
            }

            UstadTextEditField {
                value = (props.uiState.courseBlock?.cbLateSubmissionPenalty ?: 0)
                    .toString() + " %"
                label = strings[MessageID.late_submission_penalty]
                error = props.uiState.caStartDateError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                        cbLateSubmissionPenalty = it.toInt()
                    })
                }
            }

            Typography {
               + strings[MessageID.penalty_label]
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
                    cbCompletionCriteria = 14
                },
                gracePeriodVisible = true,
            )
        }
    }
}