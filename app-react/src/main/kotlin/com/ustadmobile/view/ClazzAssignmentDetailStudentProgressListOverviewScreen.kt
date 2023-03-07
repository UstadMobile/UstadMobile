package com.ustadmobile.view

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ClazzAssignmentDetailStudentProgressListOverviewUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.lib.db.entities.*
import csstype.*
import mui.icons.material.*
import mui.material.*
import mui.material.List
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create


external interface ClazzAssignmentDetailStudentProgressListOverviewScreenProps : Props {

    var uiState: ClazzAssignmentDetailStudentProgressListOverviewUiState

    var onClickPerson: (AssignmentSubmitterSummary?) -> Unit

}

val ClazzAssignmentDetailStudentProgressListOverviewScreenPreview = FC<Props> {

    ClazzAssignmentDetailStudentProgressListOverviewScreenComponent2 {
        uiState = ClazzAssignmentDetailStudentProgressListOverviewUiState(
            progressSummary = AssignmentProgressSummary().apply {
                totalStudents = 10
                submittedStudents = 2
                markedStudents = 3
            },
            assignmentSubmitterList = listOf(
                AssignmentSubmitterSummary().apply {
                    submitterUid = 1
                    name = "Bob Dylan"
                    latestPrivateComment = "Here is private comment"
                    fileSubmissionStatus = CourseAssignmentSubmission.MARKED
                },
                AssignmentSubmitterSummary().apply {
                    submitterUid = 2
                    name = "Morris Rogers"
                    latestPrivateComment = "Here is private comment"
                    fileSubmissionStatus = CourseAssignmentSubmission.SUBMITTED
                }
            ),
        )
    }
}

private val ClazzAssignmentDetailStudentProgressListOverviewScreenComponent2 =
    FC<ClazzAssignmentDetailStudentProgressListOverviewScreenProps> { props ->

        val assignmentSummaryList = listOf (
            Pair(props.uiState.progressSummary?.calculateNotSubmittedStudents(), MessageID.not_started),
            Pair(props.uiState.progressSummary?.submittedStudents, MessageID.submitted_cap),
            Pair(props.uiState.progressSummary?.markedStudents, MessageID.marked_cap)
        )

        Container {
            maxWidth = "lg"

            Stack {
                spacing = responsive(10.px)


                Stack {
                    direction = responsive(StackDirection.row)

                    assignmentSummaryList.forEachIndexed { index, summaryPair ->

                        ClazzAssignmentSummaryColumn {
                            number = summaryPair.first
                            messageID = summaryPair.second
                        }

                        if (index < 2){
                            Box {
                                sx {
                                    backgroundColor = rgb(211, 211, 211)
                                    width = 1.5.px
                                    margin = Margin(horizontal = 30.px, vertical = 5.px)
                                }
                            }
                        }
                    }
                }

                List{

                    props.uiState.assignmentSubmitterList.forEach { personItem ->
                        AssignmentDetailAttemptListItem {
                            person = personItem
                            onClick = props.onClickPerson
                        }
                    }
                }
            }
        }

}

external interface AClazzAssignmentSummaryColumnProps : Props {

    var number: Int?

    var messageID: Int

}

private val ClazzAssignmentSummaryColumn = FC<AClazzAssignmentSummaryColumnProps> {
        props ->

    val strings = useStringsXml()

    Stack {

        Typography {
            variant = TypographyVariant.h4
            + (props.number).toString()
        }

        Typography {
            + (strings[props.messageID])
        }
    }

}


external interface AssignmentDetailAttemptListItemProps : Props {

    var person: AssignmentSubmitterSummary

    var onClick: (AssignmentSubmitterSummary) -> Unit

}

private val AssignmentDetailAttemptListItem = FC<AssignmentDetailAttemptListItemProps> { props ->

    val strings = useStringsXml()

    val personUiState = props.person.listItemUiState

    val assignmentStatusIcon = ASSIGNMENT_STATUS_MAP[props.person.fileSubmissionStatus]
        ?: Done

    ListItem {
        ListItemButton {
            onClick = { props.onClick(props.person) }

            ListItemIcon {
                + AccountCircle.create {
                    sx {
                        width = 40.px
                        height = 40.px
                    }
                }
            }

            ListItemText {
                primary = ReactNode(props.person.name ?: "")
                secondary = Stack.create {
                   direction = responsive(StackDirection.row)

                    if (personUiState.latestPrivateCommentVisible){
                        Comment.create {
                            sx {
                                width = 12.px
                                height = 12.px
                            }
                        }

                        Typography {
                            + props.person.latestPrivateComment
                        }

                    }
                }
            }

        }

        secondaryAction = Stack.create {
            direction = responsive(StackDirection.row)

            if (personUiState.fileSubmissionStatusIconVisible){
                + assignmentStatusIcon.create {
                    sx {
                        width = 24.px
                        height = 24.px
                    }
                }
            }
            if (personUiState.fileSubmissionStatusTextVisible){
                Typography {
                    + (" "+strings[SubmissionConstants.STATUS_MAP[
                            props.person.fileSubmissionStatus]
                        ?: MessageID.not_submitted_cap])
                }
            }
        }
    }
}