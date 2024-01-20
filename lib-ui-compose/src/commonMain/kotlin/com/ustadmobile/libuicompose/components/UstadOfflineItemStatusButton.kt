package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ustadmobile.lib.db.composites.OfflineItemAndState

@Composable
fun UstadOfflineItemStatusIcon(
    state: OfflineItemAndState?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val icon = when {
            state?.readyForOffline == true -> Icons.Default.OfflinePin
            state?.activeDownload != null -> Icons.Default.Stop
            else -> Icons.Default.FileDownload
        }

        Icon(icon, contentDescription = null)
        state?.activeDownload?.also { jobAndTotals ->
            val progress = jobAndTotals.transferred.toFloat() / maxOf(jobAndTotals.totalSize.toFloat(), 1f)
            CircularProgressIndicator(
                progress = progress
            )
        }
    }
}