package com.ustadmobile.libuicompose.util.ext

import androidx.compose.foundation.ContextMenuItem
import com.ustadmobile.core.impl.appstate.UstadContextMenuItem

fun UstadContextMenuItem.asContextMenuItem() = ContextMenuItem(
    label = label, onClick = onClick
)

fun List<UstadContextMenuItem>.asContextMenuItems() = map {
    it.asContextMenuItem()
}

