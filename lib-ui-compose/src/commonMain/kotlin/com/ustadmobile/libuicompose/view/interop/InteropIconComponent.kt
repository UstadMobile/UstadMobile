package com.ustadmobile.libuicompose.view.interop

import androidx.compose.runtime.Composable
import com.ustadmobile.core.domain.interop.InteropIcon

@Composable
expect fun InteropIconComponent(
    interopIcon: InteropIcon,
)