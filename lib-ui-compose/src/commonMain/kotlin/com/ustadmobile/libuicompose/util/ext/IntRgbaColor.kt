package com.ustadmobile.libuicompose.util.ext

import androidx.compose.ui.graphics.Color

fun Int.rgbaColor(): Color = Color(
    red = (this shr 24) and 0xff,
    green = (this shr 16) and 0xff,
    blue = (this shr 8) and 0xff
)
