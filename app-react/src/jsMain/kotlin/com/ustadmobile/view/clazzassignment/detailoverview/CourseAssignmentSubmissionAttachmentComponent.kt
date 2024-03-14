package com.ustadmobile.view.clazzassignment.detailoverview

import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionFile
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode

external interface CourseAssignmentSubmissionAttachmentProps: Props {
    var file: CourseAssignmentSubmissionFile
}

/**
 * Used within ListItem (editable) and ListItemButton (non editable, click to open)
 */
val CourseAssignmentSubmissionAttachmentComponent = FC<CourseAssignmentSubmissionAttachmentProps> { props ->
    ListItemText {
        primary = ReactNode(props.file.casaFileName ?: "")
    }
}

