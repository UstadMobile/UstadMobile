package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import com.ustadmobile.core.impl.appstate.UstadContextMenuItem

/**
 * Expect/actual wrapper to setup ContextMenuArea (to handle right clicks) on desktop. Has no effect
 * on Android
 */
@Composable
expect fun UstadContextMenuArea(
    items: () -> List<UstadContextMenuItem>,
    content: @Composable () -> Unit,
)
