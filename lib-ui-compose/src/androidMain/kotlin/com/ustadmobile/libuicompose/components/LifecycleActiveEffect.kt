package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Use the LocalLifecycleOwner to emit an event when the component becomes active or inactive.
 * Used to track usage duration (e.g. avoid counting time when the component is not active).
 */
@Composable
fun LifecycleActiveEffect(
    onActiveChanged: (Boolean) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentlyActive by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if(event.targetState.isAtLeast(Lifecycle.State.RESUMED)) {
                currentlyActive = true
            }else {
                currentlyActive = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(currentlyActive) {
        onActiveChanged(currentlyActive)
    }
}
