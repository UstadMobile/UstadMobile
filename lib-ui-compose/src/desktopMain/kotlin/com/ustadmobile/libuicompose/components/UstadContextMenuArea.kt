package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.runtime.Composable
import com.ustadmobile.core.impl.appstate.UstadContextMenuItem
import com.ustadmobile.libuicompose.util.ext.asContextMenuItems

@Composable
actual fun UstadContextMenuArea(
    items: () -> List<UstadContextMenuItem>,
    content: @Composable () -> Unit,
) {
    ContextMenuArea(
        items = { items().asContextMenuItems() },
        content = content,
    )
}
