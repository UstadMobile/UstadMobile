package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.lib.db.composites.OfflineItemAndState
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun UstadOfflineItemStatusQuickActionButton(
    state: OfflineItemAndState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when {
        state?.readyForOffline == true -> MR.strings.remove
        state?.activeDownload != null -> MR.strings.cancel
        else -> MR.strings.download
    }

    UstadQuickActionButton(
        iconContent = {
            UstadOfflineItemStatusIcon(state)
        },
        labelText = stringResource(label),
        onClick = onClick,
        modifier = modifier,
    )
}