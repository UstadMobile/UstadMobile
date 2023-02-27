package com.ustadmobile.view

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ClazzAssignmentDetailStudentProgressListOverviewUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.lib.db.entities.*
import csstype.px
import mui.icons.material.*
import mui.material.*
import mui.material.List
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
                }
            ),
        )
    }
}

private val ClazzAssignmentDetailStudentProgressListOverviewScreenComponent2 =
    FC<ClazzAssignmentDetailStudentProgressListOverviewScreenProps> { props ->

        val strings = useStringsXml()

        Container {
            maxWidth = "lg"

            Stack {
                spacing = responsive(10.px)


                Stack {
                    direction = responsive(StackDirection.row)

                    Stack {

                        Typography {
                            + (props.uiState.progressSummary?.calculateNotSubmittedStudents() ?: 0)
                                .toString()
                        }

                        Typography {
                            + (strings[MessageID.not_started])
                        }
                    }

                    Divider {
                        orientation = Orientation.vertical
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



external interface AssignmentDetailAttemptListItemProps : Props {

    var person: AssignmentSubmitterSummary

    var onClick: (AssignmentSubmitterSummary) -> Unit

}

private val AssignmentDetailAttemptListItem = FC<AssignmentDetailAttemptListItemProps> { props ->

    val strings = useStringsXml()

    val personUiState = props.person.listItemUiState

    val assignmentStatusIcon = ASSIGNMENT_STATUS_MAP[props.person.fileSubmissionStatus]
        ?: Done.create()

    ListItem {
        ListItemButton {
            onClick = { props.onClick(props.person) }

            ListItemIcon {
                AccountCircle.create {
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

                    if (personUiState.fileSubmissionStatusIconVisible){
                        Icon {
                            + assignmentStatusIcon
                            sx {
                               width = 16.px
                               height = 16.px
                            }
                        }
                    }
                    if (personUiState.fileSubmissionStatusTextVisible){
                        Typography {
                            strings[SubmissionConstants.STATUS_MAP[
                                    props.person.fileSubmissionStatus]
                                ?: MessageID.not_submitted_cap]
                        }
                    }
                }
            }
        }
    }
}