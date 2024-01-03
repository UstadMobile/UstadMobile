package com.ustadmobile.view.clazzassignment.detail.submissionstab

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazzassignment.avatarColorName
import com.ustadmobile.core.viewmodel.clazzassignment.avatarName
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.listItemUiState
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.view.clazzassignment.detailoverview.ASSIGNMENT_STATUS_MAP
import com.ustadmobile.view.components.UstadPersonAvatar
import web.cssom.px
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

    val strings = useStringProvider()

    val personUiState = props.submitterSummary?.listItemUiState

    val assignmentStatusIcon = ASSIGNMENT_STATUS_MAP[props.submitterSummary?.fileSubmissionStatus]
        ?: DoneIcon

    ListItem {
        ListItemButton {
            onClick = {   props.submitterSummary?.also(props.onClick) }

            ListItemIcon {
                UstadPersonAvatar {
                    personName = props.submitterSummary?.avatarName()
                    pictureUri = props.submitterSummary?.pictureUri
                    colorName = props.submitterSummary?.avatarColorName()
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
                    ?: MR.strings.not_submitted_cap])
            }

        }
    }
}


