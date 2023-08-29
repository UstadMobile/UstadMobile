package com.ustadmobile.mui.components

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.clazzassignment.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.view.clazzassignment.detailoverview.ASSIGNMENT_STATUS_MAP
import web.cssom.rgb
import js.core.jso
import mui.icons.material.Done
import mui.icons.material.EmojiEvents
import mui.material.ListItem
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.SvgIconProps
import mui.material.Typography
import mui.system.Stack
import react.create
import react.*
import react.dom.html.ReactHTML.span

external interface UstadAssignmentSubmissionHeaderProps: Props {

    var uiState: UstadAssignmentSubmissionHeaderUiState

}

val UstadAssignmentSubmissionHeader = FC<UstadAssignmentSubmissionHeaderProps> {
        props ->

    val strings = useStringsXml()

    Stack{


        ListItem {
            ListItemIcon {
                Done.create()
                + (ASSIGNMENT_STATUS_MAP[props.uiState.assignmentStatus] ?: Done).create()
            }
            ListItemText {
                primary = ReactNode(strings[SubmissionConstants.STATUS_MAP
                        [props.uiState.assignmentStatus] ?: MessageID.not_submitted_cap])
                secondary = ReactNode(strings[MessageID.status])
            }
        }


        if (props.uiState.showPoints){
            val pointsElement = Typography.create {
                + (props.uiState.assignmentMark?.averageScore?.toString() ?: "")
                + "/"
                + (props.uiState.block?.cbMaxPoints?.toString() ?: "")
                + strings[MessageID.points]
                + " "

                if (props.uiState.latePenaltyVisible) {
                    span {
                        style = jso {
                            color = rgb(255, 0,0, 1.0)
                        }

                        + (strings[MessageID.late_penalty]
                            .replace("%1\$s", (
                                props.uiState.block?.cbLateSubmissionPenalty ?: 0)
                                .toString()))
                    }
                }
            }

            UstadDetailField{
                valueText = pointsElement
                labelText = strings[MessageID.xapi_result_header]
                icon = EmojiEvents.create()
            }
        }

    }
}

val UstadAssignmentFileSubmissionHeaderPreview = FC<Props> {
    UstadAssignmentSubmissionHeader {
        uiState = UstadAssignmentSubmissionHeaderUiState(
            assignmentStatus = CourseAssignmentSubmission.NOT_SUBMITTED
        )
    }
}