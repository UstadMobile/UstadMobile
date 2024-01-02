package com.ustadmobile.libuicompose.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource

/**
 * Icon to represent the status of a transfer (e.g. picture, submission attachment, etc)
 */
@Composable
fun UstadTransferStatusIcon(
    transferJobItemStatus: TransferJobItemStatus,
    modifier: Modifier = Modifier,
) {
    val (icon, stringResource) = when(transferJobItemStatus) {
        TransferJobItemStatus.IN_PROGRESS -> Icons.Default.Sync to MR.strings.in_progress
        TransferJobItemStatus.QUEUED -> Icons.Default.Schedule to MR.strings.queued
        TransferJobItemStatus.FAILED -> Icons.Default.Error to MR.strings.failed
        TransferJobItemStatus.COMPLETE -> Icons.Default.DownloadDone to MR.strings.completed
    }

    Icon(
        imageVector = icon,
        tint = MaterialTheme.colorScheme.onSurfaceVariant, //Same as ListItem supporting content
        contentDescription = stringResource(stringResource),
        modifier = modifier,
    )
}
