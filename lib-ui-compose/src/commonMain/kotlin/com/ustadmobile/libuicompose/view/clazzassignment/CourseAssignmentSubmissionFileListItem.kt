package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.ext.progressAsFloat
import com.ustadmobile.lib.db.composites.CourseAssignmentSubmissionFileAndTransferJob
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadTooltipBox

@Composable
fun CourseAssignmentSubmissionFileListItem(
    fileAndTransferJob: CourseAssignmentSubmissionFileAndTransferJob,
    onRemove: ((CourseAssignmentSubmissionFileAndTransferJob) -> Unit)? = null,
    onClickOpen: (CourseAssignmentSubmissionFileAndTransferJob) -> Unit = { },
) {
    ListItem(
        modifier = Modifier.clickable {
            onClickOpen(fileAndTransferJob)
        },
        leadingContent = {
            Icon(Icons.Default.Article, contentDescription = null)
        },
        headlineContent = {
            Text(fileAndTransferJob.submissionFile?.casaFileName ?: "")
        },
        supportingContent = {
            Column {
                fileAndTransferJob.transferJobItem?.also { transferJobItem ->
                    when(transferJobItem.tjiStatus) {
                        TransferJobItemStatus.STATUS_IN_PROGRESS_INT -> {
                            LinearProgressIndicator(
                                progress = transferJobItem.progressAsFloat,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        TransferJobItemStatus.STATUS_FAILED -> {
                            Row {
                                Icon(Icons.Default.Error, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(MR.strings.error))
                            }
                        }
                    }

                }
            }
        },
        trailingContent = onRemove?.let { onRemoveFn ->
            {
                UstadTooltipBox(
                    tooltipText = stringResource(MR.strings.remove)
                ) {
                    IconButton(
                        onClick = {
                            onRemoveFn(fileAndTransferJob)
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(MR.strings.remove))
                    }
                }
            }
        }
    )
}