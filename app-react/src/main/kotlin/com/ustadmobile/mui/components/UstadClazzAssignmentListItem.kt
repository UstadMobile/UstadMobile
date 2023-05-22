package com.ustadmobile.mui.components

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.contententry.list.listItemUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.ext.paddingCourseBlockIndent
import com.ustadmobile.view.clazzassignment.detailoverview.ASSIGNMENT_STATUS_MAP
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

    var courseBlock: CourseBlockWithCompleteEntity

    var onClickCourseBlock: (CourseBlock) -> Unit

    var padding: Padding
}

val UstadClazzAssignmentListItem = FC<UstadClazzAssignmentListItemProps> { props ->

    val assignment = props.courseBlock.assignment

    ListItem{
        ListItemButton{
            onClick = { props.onClickCourseBlock(props.courseBlock) }

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
                primary = ReactNode(props.courseBlock.cbTitle ?: "")
                secondary = SecondaryContent.create{
                    courseBlock = props.courseBlock
                }
            }
        }
    }
}

private val DateAndPointRow = FC<UstadClazzAssignmentListItemProps> { props ->

    val dateTime = useFormattedDateAndTime(
        timeInMillis = props.courseBlock.cbDeadlineDate,
        timezoneId = TimeZone.currentSystemDefault().id
    )

    val courseBlockUiState = props.courseBlock.listItemUiState

    val assignment = props.courseBlock.assignment

    val assignmentUiState = assignment?.listItemUiState

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

        if (assignmentUiState?.assignmentMarkVisible == true){
            Stack {
//To be fixed as part of the assignment screens
//                Typography {
//                    + ("${assignment.mark?.camMark ?: 0}/" +
//                            "${props.courseBlock.cbMaxPoints} " +
//                            strings[MessageID.points])
//                }
            }
        }
    }
}


private val SecondaryContent = FC<UstadClazzAssignmentListItemProps> { props ->

    val strings = useStringsXml()

    val courseBlockUiState = props.courseBlock.listItemUiState

    val assignment = props.courseBlock.assignment

    val assignmentUiState = assignment?.listItemUiState

    Stack {
        if (courseBlockUiState.cbDescriptionVisible){
            Typography{
                + (props.courseBlock.cbDescription ?: "")
            }
        }

        DateAndPointRow { courseBlock = props.courseBlock }

        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(10.px)

            if (assignmentUiState?.submissionStatusIconVisible == true){
                Icon{
                    + ASSIGNMENT_STATUS_MAP[assignment.fileSubmissionStatus]
                }
            }

            if (assignmentUiState?.submissionStatusVisible == true){
                Typography {
                    + (strings.mapLookup(
                        assignment.fileSubmissionStatus,
                        SubmissionConstants.STATUS_MAP
                    ) ?: "")
                }
            }
        }

        if (assignmentUiState?.progressTextVisible == true){
            Typography {
                + (strings[MessageID.three_num_items_with_name_with_comma]

                    .replace("%1\$d",
                        (assignment.progressSummary
                            ?.calculateNotSubmittedStudents() ?: 0).toString())

                    .replace("%2\$s", strings[MessageID.not_submitted_cap])

                    .replace("%3\$d",
                        (assignment.progressSummary
                            ?.submittedStudents ?: 0).toString())

                    .replace("%4\$s", strings[MessageID.submitted_cap])

                    .replace("%5\$d",
                        (assignment.progressSummary
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

            courseBlock = CourseBlockWithCompleteEntity().apply {
                cbTitle = "Module"
                cbDescription = "Description"
                cbDeadlineDate = 1672707505000
                cbMaxPoints = 100
                cbIndentLevel = 1
                assignment = ClazzAssignmentWithMetrics().apply {
//To be fixed as part of the assignment screens
//                    mark = CourseAssignmentMark().apply {
//                        camPenalty = 20
//                        camMark = 20F
//                    }
                    progressSummary = AssignmentProgressSummary().apply {
                        hasMetricsPermission = false
                    }
                    fileSubmissionStatus = CourseAssignmentSubmission.NOT_SUBMITTED
                }
            }

            padding = paddingCourseBlockIndent(6)
        }
    }
}