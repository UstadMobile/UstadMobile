package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Book
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.UstadAssignmentFileSubmissionListItemUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.compose.rememberFormattedDateTime
import java.util.*


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadAssignmentFileSubmissionListItem(
    modifier: Modifier = Modifier,
    uiState: UstadAssignmentFileSubmissionListItemUiState,
    onClickOpenSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit = {},
    onClickDeleteSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit = {}
){

    val formattedDateTime = rememberFormattedDateTime(
        uiState.fileSubmission.casTimestamp,
        timeZoneId = TimeZone.getDefault().id
    )

    ListItem(
        modifier = modifier
            .clickable {
                onClickOpenSubmission(uiState.fileSubmission)
            },

        icon = {
            Icon(
                Icons.Outlined.Book,
                contentDescription = "",
                modifier = Modifier.size(70.dp)
            )
        },
        text = { Text(uiState.fileNameText) },
        secondaryText = {
            if (uiState.isSubmitted){
               Text("${stringResource(R.string.submitted_cap)}:$formattedDateTime")
            }
        },
        trailing = {
            IconButton(
                onClick = { onClickDeleteSubmission(uiState.fileSubmission) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "",
                )
            }
        }
    )
}


@Composable
@Preview
private fun UstadAssignmentFileSubmissionListItemPreview() {

    UstadAssignmentFileSubmissionListItem(
        uiState = UstadAssignmentFileSubmissionListItemUiState(
            fileNameText = "Content Title",
            isSubmitted = true
        )
    )
}