package com.ustadmobile.libuicompose.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.impl.appstate.AppActionButton
import com.ustadmobile.libuicompose.util.ext.imageVector

@Composable
fun UstadActionButtonIcon(
    appActionButton: AppActionButton
) {
    UstadTooltipBox(
        tooltipText = appActionButton.contentDescription
    ) {
        IconButton(
            onClick = appActionButton.onClick,
            modifier = Modifier.testTag(appActionButton.id),
        ) {
            Icon(
                imageVector = appActionButton.icon.imageVector,
                contentDescription = appActionButton.contentDescription,
            )
        }
    }
}