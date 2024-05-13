package com.ustadmobile.libuicompose.view.interop

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.DrawablePainter
import com.ustadmobile.core.domain.interop.InteropIcon
import com.ustadmobile.core.domain.interop.InteropIconAndroid
import androidx.compose.foundation.layout.size
@Composable
actual fun InteropIconComponent(
    interopIcon: InteropIcon,
) {
    val drawable = (interopIcon as InteropIconAndroid).drawable
    val painter = remember(drawable) {
        DrawablePainter(drawable)
    }

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.size(96.dp),
    )
}