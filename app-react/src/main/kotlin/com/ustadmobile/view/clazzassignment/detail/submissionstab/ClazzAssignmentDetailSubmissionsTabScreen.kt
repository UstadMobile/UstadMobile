package com.ustadmobile.view.clazzassignment.detail.submissionstab

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabUiState
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.*
import js.core.jso
import mui.material.*
import mui.material.List
import mui.system.responsive
import react.FC
import react.Props
import react.create


external interface ClazzAssignmentDetailSubmissionsTabComponentProps : Props {

    var uiState: ClazzAssignmentDetailSubmissionsTabUiState

    var onClickPerson: (AssignmentSubmitterSummary?) -> Unit

}

val ClazzAssignmentDetailSubmissionsTabPreview = FC<Props> {

    ClazzAssignmentDetailSubmissionsTabComponent {
        uiState = ClazzAssignmentDetailSubmissionsTabUiState(
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

private val ClazzAssignmentDetailSubmissionsTabComponent = FC<ClazzAssignmentDetailSubmissionsTabComponentProps> { props ->

    val tabAndAppBarHeight = useTabAndAppBarHeight()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${tabAndAppBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item(key = "header") {
                Stack.create {
                    direction = responsive(StackDirection.row)

                    ClazzAssignmentSummaryColumn {
                        total = props.uiState.progressSummary?.calculateNotSubmittedStudents()
                        messageID = MessageID.not_started
                        showDivider = true
                    }

                    ClazzAssignmentSummaryColumn {
                        total = props.uiState.progressSummary?.submittedStudents
                        messageID = MessageID.submitted_cap
                        showDivider = true
                    }

                    ClazzAssignmentSummaryColumn {
                        total = props.uiState.progressSummary?.markedStudents
                        messageID = MessageID.submitted_cap
                    }
                }
            }

            items(
                list = props.uiState.assignmentSubmitterList,
                key = { it.submitterUid.toString() }
            ) { personItem ->
                SubmitterSummaryListItem.create {
                    submitterSummary = personItem
                    onClick = props.onClickPerson
                }
            }
        }

        Container {
            maxWidth = "lg"

            List {
                VirtualListOutlet()
            }
        }
    }

}

