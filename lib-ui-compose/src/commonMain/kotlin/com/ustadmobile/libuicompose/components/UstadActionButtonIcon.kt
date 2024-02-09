package com.ustadmobile.libuicompose.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
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
            onClick = appActionButton.onClick
        ) {
            Icon(
                imageVector = appActionButton.icon.imageVector,
                contentDescription = appActionButton.contentDescription,
            )
        }
    }
}