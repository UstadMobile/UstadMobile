package com.ustadmobile.view.clazzassignment

import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.mui.components.SingleLineTypography
import kotlinx.datetime.TimeZone
import mui.icons.material.Chat
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Stack
import mui.material.StackDirection
import mui.material.SvgIconColor
import mui.material.SvgIconSize
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import react.FC
import react.Props
import react.useMemo
import mui.icons.material.AssignmentTurnedIn as AssignmentTurnedInIcon
import mui.icons.material.Chat as ChatIcon

external interface CourseAssignmentSubmissionListItemProps: Props {
    var submission: CourseAssignmentSubmission
    var onClick: () -> Unit
}

val CourseAssignmentSubmissionListItem = FC<CourseAssignmentSubmissionListItemProps> { props ->
    val submissionPlainText = useMemo(props.submission.casText) {
        props.submission.casText?.htmlToPlainText()
    }
    val submittedFormattedDateTime = useFormattedDateAndTime(
        timeInMillis = props.submission.casTimestamp,
        timezoneId = TimeZone.currentSystemDefault().id
    )

    ListItem {
        ListItemButton {
            ListItemIcon {
                AssignmentTurnedInIcon { }
            }

            Stack {
                Typography {
                    variant = TypographyVariant.subtitle1

                    + submittedFormattedDateTime
                }

                Stack {
                    direction = responsive(StackDirection.row)

                    ChatIcon {
                        color = SvgIconColor.action
                        fontSize = SvgIconSize.small
                    }

                    SingleLineTypography {
                        variant = TypographyVariant.caption

                        + submissionPlainText
                    }
                }
            }
        }
    }
}
