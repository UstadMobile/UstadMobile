package com.ustadmobile.libuicompose.util.ext

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


/**
 * Default padding for Ustad component items: effectively 16dp from the side of the screen, 16dp
 * vertical space between items (8dp top/bottom on each item).
 */
fun Modifier.defaultItemPadding(
    start: Dp = 16.dp,
    top: Dp = 8.dp,
    end: Dp = 16.dp,
    bottom: Dp = 8.dp,
): Modifier = padding(start = start, top = top, end = end, bottom = bottom)


fun Modifier.scaledDefaultItemPadding(
    scale: Float,
    start: Dp = (16 * scale).dp,
    top: Dp = (8 * scale).dp,
    end: Dp = (16 * scale).dp,
    bottom: Dp = (8 * scale).dp,
) = padding(start = start, top = top, end = end, bottom = bottom)

/**
 * Default padding for a screen. This is 8dp at the top and bottom. Horizontal padding is handled
 * by components themselves.
 */
fun Modifier.defaultScreenPadding() = padding(horizontal = 0.dp, vertical = 8.dp)

fun Modifier.defaultAvatarSize() = size(40.dp)

fun Modifier.testTagIfNotNull(tag: String?): Modifier = this.let {
    if(tag != null)
        it.testTag(tag)
    else
        it
}
