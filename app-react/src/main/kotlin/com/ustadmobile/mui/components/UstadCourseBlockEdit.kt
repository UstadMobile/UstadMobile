package com.ustadmobile.mui.components

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.CompletionCriteriaConstants
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.common.md
import com.ustadmobile.mui.common.xs
import com.ustadmobile.util.ext.addOptionalSuffix
import csstype.JustifyContent
import csstype.TextAlign
import csstype.number
import csstype.px
import mui.icons.material.AccountCircle
import mui.icons.material.CheckBoxOutlineBlank
import mui.icons.material.Visibility
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface UstadCourseBlockEditProps: Props {

    var uiState: CourseBlockEditUiState

    var onCourseBlockChange: ((CourseBlock?) -> Unit)

    var onCaStartDateChange: ((Long?) -> Unit)?

    var onCaStartTimeChange: ((Long?) -> Unit)?

    var onDeadlineDateChange: ((Long?) -> Unit)?

    var onDeadlineTimeChange: ((Long?) -> Unit)?

    var onGracePeriodDateChange: ((Long?) -> Unit)?

    var onGracePeriodTimeChange: ((Long?) -> Unit)?
}

/**
 * Base component for showing detail fields e.g. phone number, start date, end date, etc.
 */
val UstadCourseBlockEdit = FC<UstadCourseBlockEditProps> { props ->

    val strings = useStringsXml()

    Stack{
        spacing = responsive(20.px)

        FirstRow {
            uiState = props.uiState
            onCaStartDateChange = props.onCaStartDateChange
            onCaStartTimeChange = props.onCaStartTimeChange
        }

        SecondRow{
            uiState = props.uiState
            onCourseBlockChange = props.onCourseBlockChange
        }

        UstadTextEditField {
            value = ((props.uiState.courseBlock?.cbMaxPoints ?: 0).toString() + " " +
                    strings[MessageID.points])
            label = strings[MessageID.points]
            error = props.uiState.caStartDateError
            enabled = props.uiState.fieldsEnabled
            onChange = {
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbMaxPoints = it.toInt()
                })
            }
        }

        FourthRow{
            uiState = props.uiState
            onDeadlineDateChange = props.onDeadlineDateChange
            onDeadlineTimeChange = props.onDeadlineTimeChange
        }

        if (props.uiState.gracePeriodVisible){

            FifthRow{
                uiState = props.uiState
                onGracePeriodDateChange = props.onGracePeriodDateChange
                onGracePeriodTimeChange = props.onGracePeriodTimeChange
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

val FirstRow = FC<UstadCourseBlockEditProps> { props ->

    val strings = useStringsXml()

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(15.px)

        Stack {

            UstadDateEditField {
                timeInMillis = props.uiState.startTime
                label = strings[MessageID.dont_show_before].addOptionalSuffix(strings)
                error = props.uiState.caStartDateError
                enabled = props.uiState.fieldsEnabled
                onChange = { props.onCaStartDateChange?.let { it1 -> it1(it) } }
            }

            Typography{ props.uiState.timeZone }
        }

        UstadDateEditField {
            timeInMillis = props.uiState.startTime
            label = strings[MessageID.time]
            enabled = props.uiState.fieldsEnabled
            onChange = { props.onCaStartTimeChange?.let { it1 -> it1(it) } }
        }
    }
}

val SecondRow = FC<UstadCourseBlockEditProps> { props ->

    val strings = useStringsXml()

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(15.px)

        UstadMessageIdDropDownField {
            value = props.uiState.courseBlock?.cbCompletionCriteria ?: 0
            label = strings[MessageID.completion_criteria]
            options = CompletionCriteriaConstants.COMPLETION_CRITERIA_MESSAGE_IDS
            onChange = {
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbCompletionCriteria = it?.value ?: 0
                })
            }
            enabled = props.uiState.fieldsEnabled
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
}

val FourthRow = FC<UstadCourseBlockEditProps> { props ->

    val strings = useStringsXml()

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(15.px)

        UstadDateEditField {
            timeInMillis = props.uiState.deadlineDate
            label = strings[MessageID.deadline].addOptionalSuffix(strings)
            error = props.uiState.caDeadlineError
            enabled = props.uiState.fieldsEnabled
            onChange = { props.onDeadlineDateChange?.let { it1 -> it1(it) } }
        }


        UstadDateEditField {
            timeInMillis = props.uiState.deadlineTime
            label = strings[MessageID.time]
            error = props.uiState.caDeadlineError
            enabled = props.uiState.fieldsEnabled
            onChange = { props.onDeadlineTimeChange?.let { it1 -> it1(it) } }
        }
    }
}

val FifthRow = FC<UstadCourseBlockEditProps> { props ->

    val strings = useStringsXml()

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(15.px)

        UstadDateEditField {
            timeInMillis = props.uiState.gracePeriodDate
            label = strings[MessageID.end_of_grace_period]
            error = props.uiState.caGracePeriodError
            enabled = props.uiState.fieldsEnabled
            onChange = { props.onGracePeriodDateChange?.let { it1 -> it1(it) } }
        }

        UstadDateEditField {
            timeInMillis = props.uiState.gracePeriodTime
            label = strings[MessageID.time]
            error = props.uiState.caGracePeriodError
            enabled = props.uiState.fieldsEnabled
            onChange = { props.onGracePeriodTimeChange?.let { it1 -> it1(it) } }
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
                gracePeriodDate = 1669788143000,
                gracePeriodTime = 1669788143000,
                timeZone = "Class timezone: Dubai",
                minScoreVisible = true,
                gracePeriodVisible = true,
            )
        }
    }
}