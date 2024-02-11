package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable
import com.ustadmobile.core.impl.appstate.UstadContextMenuItem

@Composable
actual fun UstadContextMenuArea(
    items: () -> List<UstadContextMenuItem>,
    content: @Composable () -> Unit,
) {
    ContextMenuArea(
        items = {
            items().map {
                ContextMenuItem(it.label, it.onClick)
            }
        },
        content = content,
    )
}
