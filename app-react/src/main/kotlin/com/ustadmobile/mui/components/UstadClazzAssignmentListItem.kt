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

            sx {
                padding = paddingCourseBlockIndent(
                    props.uiState.block.cbIndentLevel
                )
            }

            ListItemIcon {
                + AssignmentTurnedIn.create()
            }

            Box {
                sx{ width = 10.px }
            }

            ListItemText {
                primary = ReactNode(props.uiState.assignment.caTitle ?: "")
                secondary = SecondaryContent.create{
                    uiState = props.uiState
                }
            }
        }
    }
}

val DateAndPointRow = FC<UstadClazzAssignmentListItemProps> { props ->

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
            }
        }
    }
}


val SecondaryContent = FC<UstadClazzAssignmentListItemProps> { props ->

    val strings = useStringsXml()

    Stack {
        if (props.uiState.cbDescriptionVisible){
            Typography{
                + (props.uiState.block.cbDescription ?: "")
            }
        }

        DateAndPointRow {
            uiState = props.uiState
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

        if (props.uiState.progressTextVisible){
            Typography {
                + (strings[MessageID.three_num_items_with_name_with_comma]

                    .replace("%1\$d",
                        (props.uiState.assignment.progressSummary
                            ?.calculateNotSubmittedStudents() ?: 0).toString())

                    .replace("%2\$s", strings[MessageID.not_submitted_cap])

                    .replace("%3\$d",
                        (props.uiState.assignment.progressSummary
                            ?.submittedStudents ?: 0).toString())

                    .replace("%4\$s", strings[MessageID.submitted_cap])

                    .replace("%5\$d",
                        (props.uiState.assignment.progressSummary
                            ?.markedStudents ?: 0).toString())

                    .replace("%6\$s", strings[MessageID.marked]))
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
                        hasMetricsPermission = true
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