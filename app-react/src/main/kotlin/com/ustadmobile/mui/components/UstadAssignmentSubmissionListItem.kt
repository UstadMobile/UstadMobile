package com.ustadmobile.mui.components

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.*
import csstype.Padding
import csstype.px
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

    var submission: CourseAssignmentSubmissionWithAttachment

    var padding: Padding

    var onClickOpenSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit

    var onClickDeleteSubmission: ((CourseAssignmentSubmissionWithAttachment) -> Unit)?

}

val UstadAssignmentSubmissionListItem = FC<UstadAssignmentSubmissionListItemProps> {
        props ->

    val formattedDateTime = useFormattedDateAndTime(
        timeInMillis = props.submission.casTimestamp,
        timezoneId = TimeZone.currentSystemDefault().id
    )

    val strings = useStringsXml()

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
                primary = ReactNode(props.submission.displayTitle)

                if (props.submission.casTimestamp.isSetDate()){

                    secondary = ReactNode(
                        "${strings[MessageID.submitted_cap]}: $formattedDateTime")

                }
            }
        }

        val onClickDeleteSubmission = props.onClickDeleteSubmission
        if (onClickDeleteSubmission != null) {
            secondaryAction = IconButton.create {
                ariaLabel = strings[MessageID.delete]
                onClick = {
                    onClickDeleteSubmission(props.submission)
                }
                Delete {}
            }
        }
    }
}


val UstadAssignmentFileSubmissionListItemPreview = FC<Props> {

    UstadAssignmentSubmissionListItem {
        submission = CourseAssignmentSubmissionWithAttachment().apply {
            casTimestamp = 1677744388299
            casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
            attachment = CourseAssignmentSubmissionAttachment().apply {
                casaFileName = "Content Title"
            }
        }
    }

}