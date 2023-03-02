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

external interface UstadAssignmentFileSubmissionListItemProps: Props {

    var submission: CourseAssignmentSubmissionWithAttachment

    var padding: Padding

    var onClickOpenSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit

    var onClickDeleteSubmission: ((CourseAssignmentSubmissionWithAttachment) -> Unit)?

}

val UstadAssignmentFileSubmissionListItem = FC<UstadAssignmentFileSubmissionListItemProps> {
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
                    sx {
                        width = 70.px
                        height = 70.px
                    }
                }

            }

            ListItemText {
                primary = ReactNode(props.submission.displayTitle)

                if (props.submission.casTimestamp.isSetDate()){

                    secondary = ReactNode(
                        "${strings[MessageID.submitted_cap]}:$formattedDateTime")

                }
            }
        }

        if (props.onClickDeleteSubmission != null) {
            secondaryAction = IconButton.create {
                onClick = { props.onClickDeleteSubmission?.let {
                        it1 -> it1(props.submission)
                } }
                Delete {}
            }
        }
    }
}


val UstadAssignmentFileSubmissionListItemPreview = FC<Props> {

    UstadAssignmentFileSubmissionListItem {
        submission = CourseAssignmentSubmissionWithAttachment().apply {

        }
    }

}