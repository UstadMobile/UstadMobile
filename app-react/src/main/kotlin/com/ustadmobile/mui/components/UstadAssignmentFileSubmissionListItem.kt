package com.ustadmobile.mui.components

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.UstadAssignmentFileSubmissionListItemUiState
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

    var uiState: UstadAssignmentFileSubmissionListItemUiState

    var padding: Padding

    var onClickOpenSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit

    var onClickDeleteSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit

}

val UstadAssignmentFileSubmissionListItem = FC<UstadAssignmentFileSubmissionListItemProps> {
        props ->

    val formattedDateTime = useFormattedDateAndTime(
        timeInMillis = props.uiState.fileSubmission.casTimestamp,
        timezoneId = TimeZone.currentSystemDefault().id
    )

    val strings = useStringsXml()

    ListItem{
        ListItemButton{
            onClick = { props.onClickOpenSubmission(props.uiState.fileSubmission) }

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
                primary = ReactNode(props.uiState.fileNameText)

                if (props.uiState.isSubmitted){

                    secondary = ReactNode(
                        "${strings[MessageID.submitted_cap]}:$formattedDateTime")

                }
            }
        }

        if (!props.uiState.isSubmitted) {
            secondaryAction = IconButton.create {
                onClick = { props.onClickDeleteSubmission(props.uiState.fileSubmission) }
                Delete {}
            }
        }
    }
}


val UstadAssignmentFileSubmissionListItemPreview = FC<Props> {

    UstadAssignmentFileSubmissionListItem {
        uiState = UstadAssignmentFileSubmissionListItemUiState(
            fileNameText = "Content Title"
        )
    }

}