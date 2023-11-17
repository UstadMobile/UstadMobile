package com.ustadmobile.libuicompose.util.ext

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ustadmobile.door.util.systemTimeInMillis


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

/**
 * Default padding for a screen. This is 8dp at the top and bottom. Horizontal padding is handled
 * by components themselves.
 */
fun Modifier.defaultScreenPadding() = padding(horizontal = 0.dp, vertical = 8.dp)

fun Modifier.defaultAvatarSize() = size(40.dp)

private const val FOCUS_TAB_THRESHOLD_TIME = 250

/**
 * Manage handling focus when user uses tab, shift+tab, etc. Required for text input fields where
 * the user should be able to move between fields using the tab key.
 */
@Composable
fun Modifier.onPreviewKeyEventFocusHandler(
    focusManager: FocusManager,
    onTab: (FocusManager) -> Boolean = {
        it.moveFocus(FocusDirection.Next)
        true
    },
    onShiftTab: (FocusManager) -> Boolean = {
        it.moveFocus(FocusDirection.Previous)
        true
    },
    onEnter: (FocusManager) -> Boolean = {
        false
    }
) : Modifier {
    var focusTime: Long by remember {
        mutableStateOf(systemTimeInMillis())
    }

    /**
     * We need to avoid triggering the onTab handlers in response to the tab event that brings the
     * user into the textfield itself. This isn't an ideal solution, but there doesn't seem to be
     * anything better or recommended best practice.
     */
    return onPreviewKeyEvent {
        val hasFocusVal = (systemTimeInMillis() - focusTime) > FOCUS_TAB_THRESHOLD_TIME
        when {
            hasFocusVal && it.key == Key.Tab && it.isShiftPressed -> onShiftTab(focusManager)
            hasFocusVal && it.key == Key.Tab -> onTab(focusManager)
            hasFocusVal && it.key == Key.Enter -> onEnter(focusManager)
            else -> false
        }
    }.onFocusChanged {
        focusTime = systemTimeInMillis()
    }
}
