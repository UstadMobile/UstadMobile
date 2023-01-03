package com.ustadmobile.mui.components

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ClazzAssignmentUiState
import com.ustadmobile.hooks.useFormattedDate
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.ext.paddingCourseBlockIndent
import com.ustadmobile.view.ASSIGNMENT_STATUS_MAP
import csstype.*
import mui.icons.material.AssignmentTurnedIn
import mui.icons.material.CalendarToday
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface UstadClazzAssignmentListItemProps: Props {

    var uiState: ClazzAssignmentUiState

    var onClickAssignment: (ClazzAssignmentWithMetrics?) -> Unit

}

val UstadClazzAssignmentListItem = FC<UstadClazzAssignmentListItemProps> { props ->

    val strings = useStringsXml()
    ListItem{
        ListItemButton{
            onClick = { props.onClickAssignment(props.uiState.assignment) }

            ListItemIcon {
                sx {
                    padding = paddingCourseBlockIndent(
                        props.uiState.block.cbIndentLevel
                    )
                }

                + AssignmentTurnedIn.create()
            }

            Box {
                sx{ width = 10.px }
            }

            ListItemText {
                primary = ReactNode(props.uiState.assignment.caTitle ?: "")
                secondary = Stack.create {
                    if (props.uiState.cbDescriptionVisible){
                        Typography{
                            + (props.uiState.block.cbDescription ?: "")
                        }
                    }

                    DateAndPointRow {
                        uiState = props.uiState
                        onClickAssignment = props.onClickAssignment
                    }

                    Stack {
                        direction = responsive(StackDirection.row)
                        spacing = responsive(10.px)

                        if (props.uiState.submissionStatusIconVisible){
                            Icon{
                                + ASSIGNMENT_STATUS_MAP[
                                        props.uiState.assignment.fileSubmissionStatus
                                ]
                            }
                        }

                        if (props.uiState.submissionStatusVisible){
                            Typography {
                               + (strings.mapLookup(
                                   props.uiState.assignment.fileSubmissionStatus,
                                   SubmissionConstants.STATUS_MAP
                                ) ?: "")
                            }
                        }
                    }
                }
            }
        }
    }
}

external interface DateAndPointRowProps: Props {

    var uiState: ClazzAssignmentUiState

    var onClickAssignment: (ClazzAssignmentWithMetrics?) -> Unit

}

val DateAndPointRow = FC<DateAndPointRowProps> { props ->

    val strings = useStringsXml()
    val dateTime = useFormattedDate(
        timeInMillis = props.uiState.block.cbDeadlineDate,
        timezoneId = props.uiState.timeZone)

    Stack {
        direction = responsive(StackDirection.row)

        if (props.uiState.cbDeadlineDateVisible){
            Icon{
                + CalendarToday.create()
            }

            Box{
                sx{ width = 2.px }
            }

            Typography{
                + dateTime
            }
        }

        Box{
            sx{ width = 5.px }
        }

        if (props.uiState.assignmentMarkVisible){
            Stack {
                Typography {
                    + ("${props.uiState.assignment.mark?.camMark ?: 0}/" +
                            "${props.uiState.block.cbMaxPoints} " +
                            strings[MessageID.points])
                }
                if (props.uiState.assignmentPenaltyVisible){
                    strings[MessageID.late_penalty]
                        .replace("%1\$d",
                            (props.uiState.block.cbLateSubmissionPenalty).toString()
                        )
                }
            }
        }
    }
}


val UstadClazzAssignmentListItemPreview = FC<Props> {
    Container {
        maxWidth = "lg"

        UstadClazzAssignmentListItem {
            uiState = ClazzAssignmentUiState(
                assignment = ClazzAssignmentWithMetrics().apply {
                    caTitle = "Module"
                    mark = CourseAssignmentMark().apply {
                        camPenalty = 20
                        camMark = 20F
                    }
                    progressSummary = AssignmentProgressSummary().apply {
                        hasMetricsPermission = false
                    }
                    fileSubmissionStatus = 2
                },
                block = CourseBlockWithCompleteEntity().apply {
                    cbDescription = "Description"
                    cbDeadlineDate = 1672707505000
                    cbMaxPoints = 100
                    cbIndentLevel = 1
                }
            )
        }
    }
}