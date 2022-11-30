package com.ustadmobile.mui.components

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.CompletionCriteriaConstants
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.util.ext.addOptionalSuffix
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

        FirstRow {
            uiState = props.uiState
            onCourseBlockChange = props.onCourseBlockChange
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
            onCourseBlockChange = props.onCourseBlockChange
        }

        if (props.uiState.gracePeriodVisible){

            FifthRow{
                uiState = props.uiState
                onCourseBlockChange = props.onCourseBlockChange
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
                timeInMillis = props.uiState.courseBlock?.cbHideUntilDate ?: 0
                label = strings[MessageID.dont_show_before].addOptionalSuffix(strings)
                error = props.uiState.caStartDateError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                        cbHideUntilDate = it
                    })
                }
            }

            Typography{
                + "timeZone"
            }
        }

        UstadDateEditField {
            timeInMillis = props.uiState.courseBlock?.cbHideUntilDate ?: 0
            label = strings[MessageID.time]
            enabled = props.uiState.fieldsEnabled
            onChange = {
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbHideUntilDate = it
                })
            }
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
            timeInMillis = props.uiState.courseBlock?.cbDeadlineDate ?: 0
            label = strings[MessageID.deadline].addOptionalSuffix(strings)
            error = props.uiState.caDeadlineError
            enabled = props.uiState.fieldsEnabled
            onChange = {
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbDeadlineDate = it
                })
            }
        }


        UstadDateEditField {
            timeInMillis = props.uiState.courseBlock?.cbDeadlineDate ?: 0
            label = strings[MessageID.time]
            error = props.uiState.caDeadlineError
            enabled = props.uiState.fieldsEnabled
            onChange = {
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbDeadlineDate = it
                })
            }
        }
    }
}

val FifthRow = FC<UstadCourseBlockEditProps> { props ->

    val strings = useStringsXml()

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(15.px)

        UstadDateEditField {
            timeInMillis = props.uiState.courseBlock?.cbGracePeriodDate ?: 0
            label = strings[MessageID.end_of_grace_period]
            error = props.uiState.caGracePeriodError
            enabled = props.uiState.fieldsEnabled
            onChange = {
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbGracePeriodDate = it
                })
            }
        }

        UstadDateEditField {
            timeInMillis = props.uiState.courseBlock?.cbGracePeriodDate ?: 0
            label = strings[MessageID.time]
            error = props.uiState.caGracePeriodError
            enabled = props.uiState.fieldsEnabled
            onChange = {
                props.onCourseBlockChange(props.uiState.courseBlock?.shallowCopy {
                    cbGracePeriodDate = it
                })
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
                minScoreVisible = true,
                gracePeriodVisible = true,
            )
        }
    }
}