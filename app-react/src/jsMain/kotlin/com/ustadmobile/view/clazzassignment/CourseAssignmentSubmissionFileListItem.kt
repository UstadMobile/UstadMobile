package com.ustadmobile.view.clazzassignment

import com.ustadmobile.lib.db.composites.CourseAssignmentSubmissionFileAndTransferJob
import react.FC
import react.Props
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import react.ReactNode

external interface CourseAssignmentSubmissionFileListItemProps : Props {
    var file: CourseAssignmentSubmissionFileAndTransferJob
}

val CourseAssignmentSubmissionFileListItem = FC<CourseAssignmentSubmissionFileListItemProps> { props ->
    ListItem {
        ListItemButton {
            ListItemText {
                primary = ReactNode(props.file.submissionFile?.casaFileName ?: "")
            }
        }
    }
}
