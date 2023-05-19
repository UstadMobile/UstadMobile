package com.ustadmobile.core.util.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Cancels a given existing job if not null, and then creates a new job with a delay. This is
 * useful for triggering a commit to savedstate when the user makes changes without converting
 * to JSON on every keystroke. For example this can trigger a commit to the SavedStateHandle 100ms
 * after the user has stopped making changes.
 *
 * e.g.
 *
 * var saveStateJob: Job? = null
 *
 * fun onEntityChanged() {
 *     saveStateJob = viewModelScope.resetAndLaunchDelayed(savedStateJob, 100) {
 *        commitEntity()
 *     }
 * }
 *
 * @param prevJob The existing job that will be canceled if it is not null
 * @param delay delay in ms
 * @param block block to run after delay
 */
fun CoroutineScope.cancelPrevJobAndLaunchDelayed(
    prevJob: Job?,
    delay: Long,
    block: suspend () -> Unit,
) : Job {
    prevJob?.cancel()
    return launch {
        delay(delay)
        block()
    }
}
