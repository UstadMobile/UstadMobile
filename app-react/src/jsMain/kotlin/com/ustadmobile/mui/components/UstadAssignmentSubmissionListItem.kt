package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.*
import web.cssom.Padding
import kotlinx.datetime.TimeZone
import mui.icons.material.BookOutlined
import mui.icons.material.Delete
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.aria.ariaLabel

external interface UstadAssignmentSubmissionListItemProps: Props {

    var submission: CourseAssignmentSubmission

    var padding: Padding

    var onClickOpenSubmission: (CourseAssignmentSubmission) -> Unit

    var onClickDeleteSubmission: ((CourseAssignmentSubmission) -> Unit)?

}

val UstadAssignmentSubmissionListItem = FC<UstadAssignmentSubmissionListItemProps> {
        props ->

    val formattedDateTime = useFormattedDateAndTime(
        timeInMillis = props.submission.casTimestamp,
        timezoneId = TimeZone.currentSystemDefault().id
    )

    val strings = useStringProvider()

    ListItem{
        ListItemButton{
            onClick = { props.onClickOpenSubmission(props.submission) }

            sx {
                padding = props.padding
            }

            ListItemIcon {
                BookOutlined {

                }

            }

            ListItemText {
                primary = ReactNode(props.submission.casText ?: "")

                if (props.submission.casTimestamp.isSetDate()){

                    secondary = ReactNode(
                        "${strings[MR.strings.submitted_cap]}: $formattedDateTime")

                }
            }
        }

        val onClickDeleteSubmission = props.onClickDeleteSubmission
        if (onClickDeleteSubmission != null) {
            secondaryAction = IconButton.create {
                ariaLabel = strings[MR.strings.delete]
                onClick = {
                    onClickDeleteSubmission(props.submission)
                }
                Delete {}
            }
        }
    }
}
