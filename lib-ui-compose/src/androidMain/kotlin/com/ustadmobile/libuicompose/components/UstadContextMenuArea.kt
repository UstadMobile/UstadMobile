package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import com.ustadmobile.core.impl.appstate.UstadContextMenuItem

@Composable
actual fun UstadContextMenuArea(
    items: () -> List<UstadContextMenuItem>,
    content: @Composable () -> Unit,
) {
    content()
}
