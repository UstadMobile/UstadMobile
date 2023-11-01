package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Book
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import java.util.*


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadAssignmentSubmissionListItem(
    modifier: Modifier = Modifier,
    submission: CourseAssignmentSubmissionWithAttachment,
    onClickOpenSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit = {},
    onClickDeleteSubmission: ((CourseAssignmentSubmissionWithAttachment) -> Unit)? = null
){

    val formattedDateTime = rememberFormattedDateTime(
        submission.casTimestamp,
        timeZoneId = TimeZone.getDefault().id
    )

    ListItem(
        modifier = modifier
            .clickable {
                onClickOpenSubmission(submission)
            },

        icon = {
            Icon(
                Icons.Outlined.Book,
                contentDescription = "",
                modifier = Modifier.size(70.dp)
            )
        },
        text = { Text(submission.displayTitle) },
        secondaryText = {
            if (submission.casTimestamp.isDateSet()){
                Text("${stringResource(MR.strings.submitted_cap)}: $formattedDateTime")
            }
        },
        trailing = {
            if (onClickDeleteSubmission != null) {
                IconButton(
                    onClick = { onClickDeleteSubmission(submission) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(MR.strings.delete),
                    )
                }
            }
        }
    )
}


//@Composable
//@Preview
//private fun UstadAssignmentSubmissionListItemPreview() {
//
//    UstadAssignmentSubmissionListItem(
//        submission = CourseAssignmentSubmissionWithAttachment().apply {
//            casTimestamp = 1677744388299
//            casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
//            attachment = CourseAssignmentSubmissionAttachment().apply {
//                casaFileName = "Content Title"
//            }
//        }
//    )
//}