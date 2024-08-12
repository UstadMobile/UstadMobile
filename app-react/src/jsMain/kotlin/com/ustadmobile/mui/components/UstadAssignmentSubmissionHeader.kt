package com.ustadmobile.mui.components

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission.Companion.NOT_SUBMITTED
import com.ustadmobile.view.clazzassignment.detailoverview.ASSIGNMENT_STATUS_MAP
import web.cssom.rgb
import js.objects.jso
import mui.icons.material.Done as DoneIcon
import mui.icons.material.EmojiEvents as EmojiEventsIcon
import mui.material.ListItem
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Typography
import mui.system.Stack
import react.create
import react.*
import react.dom.html.ReactHTML.span

external interface UstadAssignmentSubmissionHeaderProps: Props {

    var uiState: ClazzAssignmentDetailOverviewUiState

}

val UstadAssignmentSubmissionHeader = FC<UstadAssignmentSubmissionHeaderProps> {
        props ->

    val strings = useStringProvider()

    Stack{
        if(props.uiState.activeUserIsSubmitter) {
            ListItem {
                ListItemIcon {
                    + (ASSIGNMENT_STATUS_MAP[props.uiState.submissionStatus ?: NOT_SUBMITTED] ?: DoneIcon).create()
                }
                ListItemText {
                    primary = ReactNode(strings[SubmissionConstants.STATUS_MAP
                        [props.uiState.submissionStatus ?: NOT_SUBMITTED] ?: MR.strings.not_submitted_cap])
                    secondary = ReactNode(strings[MR.strings.status])
                }
            }
        }

        if (props.uiState.pointsVisible) {
            val pointsElement = Typography.create {
                + (props.uiState.submissionMark?.averageScore?.toString() ?: "")
                + "/"
                + (props.uiState.courseBlock?.cbMaxPoints?.toString() ?: "")
                + " "
                + strings[MR.strings.points]
                + " "

                if (props.uiState.latePenaltyVisible) {
                    span {
                        style = jso {
                            color = rgb(255, 0,0, 1.0)
                        }

                        + strings.format(MR.strings.late_penalty,
                            (props.uiState.courseBlock?.cbLateSubmissionPenalty ?: 0).toString() + "%"
                        )
                    }
                }
            }

            UstadDetailField2 {
                valueContent = pointsElement
                labelContent = ReactNode(strings[MR.strings.xapi_result_header])
                leadingContent = EmojiEventsIcon.create()
            }
        }

    }
}
