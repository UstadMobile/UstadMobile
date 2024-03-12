package com.ustadmobile.view.clazzassignment.detailoverview

import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionFile
import com.ustadmobile.lib.db.entities.TransferJobItem
import mui.material.ListItem
import react.FC
import react.Props

external interface EditableCourseAssignmentSubmissionFileListItemProps: Props {
    var file: CourseAssignmentSubmissionFile

}

val EditableCourseAssignmentSubmissionFileListItem = FC<EditableCourseAssignmentSubmissionFileListItemProps> {props ->
    ListItem {
        CourseAssignmentSubmissionAttachmentComponent {
            file = props.file
        }
    }
}

