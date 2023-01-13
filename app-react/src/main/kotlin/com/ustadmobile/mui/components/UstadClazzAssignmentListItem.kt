package com.ustadmobile.mui.components

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.ext.paddingCourseBlockIndent
import com.ustadmobile.view.ASSIGNMENT_STATUS_MAP
import csstype.*
import kotlinx.datetime.TimeZone
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

    var assignment: ClazzAssignmentWithMetrics

    var courseBlock: CourseBlockWithCompleteEntity

    var onClickAssignment: (ClazzAssignmentWithMetrics?) -> Unit

    var padding: Padding
}

val UstadClazzAssignmentListItem = FC<UstadClazzAssignmentListItemProps> { props ->

    ListItem{
        ListItemButton{
            onClick = { props.onClickAssignment(props.assignment) }

            sx {
                padding = props.padding
            }

            ListItemIcon {
                AssignmentTurnedIn {
                    sx {
                        width = 40.px
                        height = 40.px
                    }
                }
            }

            Box {
                sx{ width = 10.px }
            }

            ListItemText {
                primary = ReactNode(props.assignment.caTitle ?: "")
                secondary = SecondaryContent.create{
                    assignment = props.assignment
                    courseBlock = props.courseBlock
                }
            }
        }
    }
}

private val DateAndPointRow = FC<UstadClazzAssignmentListItemProps> { props ->

    val strings = useStringsXml()
    val dateTime = useFormattedDateAndTime(
        timeInMillis = props.courseBlock.cbDeadlineDate,
        timezoneId = TimeZone.currentSystemDefault().id
    )

    val courseBlockUiState = props.courseBlock.listItemUiState

    val assignmentUiState = props.assignment.listItemUiState

    Stack {
        direction = responsive(StackDirection.row)

        if (courseBlockUiState.cbDeadlineDateVisible){
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
            sx{ width = 15.px }
        }

        if (assignmentUiState.assignmentMarkVisible){
            Stack {
                Typography {
                    + ("${props.assignment.mark?.camMark ?: 0}/" +
                            "${props.courseBlock.cbMaxPoints} " +
                            strings[MessageID.points])
                }
            }
        }
    }
}


private val SecondaryContent = FC<UstadClazzAssignmentListItemProps> { props ->

    val strings = useStringsXml()

    val courseBlockUiState = props.courseBlock.listItemUiState

    val assignmentUiState = props.assignment.listItemUiState

    Stack {
        if (courseBlockUiState.cbDescriptionVisible){
            Typography{
                + (props.courseBlock.cbDescription ?: "")
            }
        }

        DateAndPointRow {
            assignment = props.assignment
            courseBlock = props.courseBlock
        }

        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(10.px)

            if (assignmentUiState.submissionStatusIconVisible){
                Icon{
                    + ASSIGNMENT_STATUS_MAP[
                            props.assignment.fileSubmissionStatus
                    ]
                }
            }

            if (assignmentUiState.submissionStatusVisible){
                Typography {
                    + (strings.mapLookup(
                        props.assignment.fileSubmissionStatus,
                        SubmissionConstants.STATUS_MAP
                    ) ?: "")
                }
            }
        }

        if (assignmentUiState.progressTextVisible){
            Typography {
                + (strings[MessageID.three_num_items_with_name_with_comma]

                    .replace("%1\$d",
                        (props.assignment.progressSummary
                            ?.calculateNotSubmittedStudents() ?: 0).toString())

                    .replace("%2\$s", strings[MessageID.not_submitted_cap])

                    .replace("%3\$d",
                        (props.assignment.progressSummary
                            ?.submittedStudents ?: 0).toString())

                    .replace("%4\$s", strings[MessageID.submitted_cap])

                    .replace("%5\$d",
                        (props.assignment.progressSummary
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

            assignment = ClazzAssignmentWithMetrics().apply {
                caTitle = "Module"
                mark = CourseAssignmentMark().apply {
                    camPenalty = 20
                    camMark = 20F
                }
                progressSummary = AssignmentProgressSummary().apply {
                    hasMetricsPermission = false
                }
                fileSubmissionStatus = CourseAssignmentSubmission.NOT_SUBMITTED
            }

            courseBlock = CourseBlockWithCompleteEntity().apply {
                cbDescription = "Description"
                cbDeadlineDate = 1672707505000
                cbMaxPoints = 100
                cbIndentLevel = 1
            }

            padding = paddingCourseBlockIndent(6)
        }
    }
}