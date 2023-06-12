package com.ustadmobile.view.clazzassignment.detail.submissionstab

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.listItemUiState
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.view.clazzassignment.detailoverview.ASSIGNMENT_STATUS_MAP
import csstype.px
import mui.icons.material.AccountCircle as AccountCircleIcon
import mui.icons.material.Comment as CommentIcon
import mui.icons.material.Done as DoneIcon
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Stack
import mui.material.StackDirection
import mui.material.Typography
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface SubmitterSummaryListItemProps : Props {

    var submitterSummary: AssignmentSubmitterSummary?

    var onClick: (AssignmentSubmitterSummary) -> Unit

}

val SubmitterSummaryListItem = FC<SubmitterSummaryListItemProps> { props ->

    val strings = useStringsXml()

    val personUiState = props.submitterSummary?.listItemUiState

    val assignmentStatusIcon = ASSIGNMENT_STATUS_MAP[props.submitterSummary?.fileSubmissionStatus]
        ?: DoneIcon

    ListItem {
        ListItemButton {
            onClick = {   props.submitterSummary?.also(props.onClick) }

            ListItemIcon {
                + AccountCircleIcon.create {
                    sx {
                        width = 40.px
                        height = 40.px
                    }
                }
            }

            ListItemText {
                primary = ReactNode(props.submitterSummary?.name ?: "")
                secondary = Stack.create {
                    direction = responsive(StackDirection.row)

                    if (personUiState?.latestPrivateCommentVisible == true){
                        CommentIcon.create {
                            sx {
                                width = 12.px
                                height = 12.px
                            }
                        }

                        Typography {
                            + props.submitterSummary?.latestPrivateComment
                        }

                    }
                }
            }

        }

        secondaryAction = Stack.create {
            direction = responsive(StackDirection.row)

            if (personUiState?.fileSubmissionStatusIconVisible == true){
                + assignmentStatusIcon.create {
                    sx {
                        width = 24.px
                        height = 24.px
                    }
                }
            }

            Typography {
                + (" "+strings[SubmissionConstants.STATUS_MAP[
                    props.submitterSummary?.fileSubmissionStatus]
                    ?: MessageID.not_submitted_cap])
            }

        }
    }
}

