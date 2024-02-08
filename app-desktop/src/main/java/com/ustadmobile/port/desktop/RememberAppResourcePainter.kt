package com.ustadmobile.port.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream

@Composable
fun rememberAppResourcePainter(path: String) : BitmapPainter? {
    return remember {
        ustadAppResourcesDir().let {
            Paths.get(it.absolutePath, path)
        }.takeIf { it.exists() }
            ?.inputStream()
            ?.buffered()
            ?.use { BitmapPainter(loadImageBitmap(it)) }
    }
}
