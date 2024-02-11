package com.ustadmobile.libuicompose.util.ext

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.ui.graphics.vector.ImageVector
import com.ustadmobile.core.impl.appstate.AppStateIcon

val AppStateIcon.imageVector: ImageVector
    get() = when(this) {
        AppStateIcon.MOVE -> Icons.Default.DriveFileMove
        AppStateIcon.CLOSE -> Icons.Default.Close
        AppStateIcon.DELETE -> Icons.Default.Delete
    }
