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
import react.ReactNode
import react.create
import mui.icons.material.ExpandMore as ExpandMoreIcon

external interface CourseAssignmentSubmissionProps: Props {
    var submission: CourseAssignmentSubmission
}

val CourseAssignmentSubmissionComponent = FC<CourseAssignmentSubmissionProps> { props ->
    val theme by useRequiredContext(ThemeContext)
    val strings = useStringProvider()
    val submittedTime = useFormattedDateAndTime(
        timeInMillis = props.submission.casTimestamp,
        timezoneId = TimeZone.currentSystemDefault().id,
    )

    Stack {
        direction = responsive(StackDirection.column)

        ListItem {
            ListItemButton {
                disableGutters = true
                ListItemText {
                    primary = ReactNode("${strings[MR.strings.your_submission]} ($submittedTime)")
                }
            }

            secondaryAction = IconButton.create {
                ExpandMoreIcon()
            }
        }

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