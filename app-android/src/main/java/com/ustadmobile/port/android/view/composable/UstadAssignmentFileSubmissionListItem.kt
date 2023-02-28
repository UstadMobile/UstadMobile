package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.UstadAssignmentFileSubmissionListItemUiState
import com.ustadmobile.lib.db.entities.*
import java.util.*


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadAssignmentFileSubmissionListItem(
    modifier: Modifier = Modifier,
    uiState: UstadAssignmentFileSubmissionListItemUiState,
    onClickOpenSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit = {}
){

    ListItem(
        modifier = modifier
            .clickable {
                onClickOpenSubmission(uiState.fileSubmission)
            },

        icon = {
            Icon(
                Icons.Default.Book,
                contentDescription = "",
                modifier = Modifier.size(70.dp)
            )
        },
        text = { Text(uiState.fileNameText) },
        secondaryText = {}
    )
}


@Composable
@Preview
private fun UstadAssignmentFileSubmissionListItemPreview() {

    UstadAssignmentFileSubmissionListItem(
        uiState = UstadAssignmentFileSubmissionListItemUiState(

        )
    )
}