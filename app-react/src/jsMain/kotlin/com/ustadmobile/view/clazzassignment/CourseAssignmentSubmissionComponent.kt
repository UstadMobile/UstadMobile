package com.ustadmobile.view.clazzassignment

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadRawHtml
import kotlinx.datetime.TimeZone
import mui.material.Box
import mui.system.sx
import react.FC
import react.Props
import react.useRequiredContext
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import com.ustadmobile.core.MR
import mui.material.IconButton
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import mui.material.Tooltip
import react.ReactNode
import react.create
import react.dom.aria.ariaLabel
import mui.icons.material.ExpandMore as ExpandMoreIcon
import mui.icons.material.ExpandLess as ExpandLessIcon

external interface CourseAssignmentSubmissionProps: Props {
    var submission: CourseAssignmentSubmission

    var submissionNum: Int

    var isCollapsed: Boolean

    var onToggleExpandCollapse: () -> Unit
}

val CourseAssignmentSubmissionComponent = FC<CourseAssignmentSubmissionProps> { props ->
    val theme by useRequiredContext(ThemeContext)
    val strings = useStringProvider()
    val submittedTime = useFormattedDateAndTime(
        timeInMillis = props.submission.casTimestamp,
        timezoneId = TimeZone.currentSystemDefault().id,
    )
    val expandLabel = if(props.isCollapsed) strings[MR.strings.expand] else strings[MR.strings.collapse]

    Stack {
        direction = responsive(StackDirection.column)

        ListItem {
            ListItemButton {
                disableGutters = true
                onClick = {
                    props.onToggleExpandCollapse()
                }

                ListItemText {
                    primary = ReactNode("${strings[MR.strings.submission]} ${props.submissionNum}")
                    secondary = ReactNode(submittedTime)
                }
            }

            secondaryAction = Tooltip.create {
                title = ReactNode(expandLabel)
                IconButton {
                    ariaLabel = expandLabel
                    onClick = {
                        props.onToggleExpandCollapse()
                    }

                    if(props.isCollapsed) {
                        ExpandMoreIcon()
                    }else {
                        ExpandLessIcon()
                    }
                }
            }
        }

        if(!props.isCollapsed) {
            Box {
                sx {
                    padding = theme.spacing(4)
                }

                UstadRawHtml {
                    html = props.submission.casText ?: ""
                }
            }
        }
    }
}