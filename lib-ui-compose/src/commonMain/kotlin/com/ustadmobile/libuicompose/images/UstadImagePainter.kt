package com.ustadmobile.libuicompose.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

enum class UstadImage {
    ILLUSTRATION_CONNECT,
    ILLUSTRATION_OFFLINE_USAGE,
    ILLUSTRATION_OFFLINE_SHARING,
    ILLUSTRATION_ORGANIZED,
}

/**
 * Various SVGs seem to work fine on Android (via Drawable XML) but don't work on JVM as expected.
 *
 * This expect/actual allows us to use the drawable resource XML on Android, and use a PNG on the
 * desktop.
 */
@Composable
expect fun ustadAppImagePainter(image: UstadImage): Painter

