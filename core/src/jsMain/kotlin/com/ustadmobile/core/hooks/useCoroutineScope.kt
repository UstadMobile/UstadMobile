package com.ustadmobile.core.hooks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import react.useEffect
import react.useMemo

/**
 * Create a coroutine scope via the useMemo hook. Cancel the coroutine scope in the cleanup.
 */
fun useCoroutineScope(vararg dependencies: Any?): CoroutineScope {
    val coroutineScope = useMemo(dependencies = dependencies) {
        CoroutineScope(Dispatchers.Main + Job())
    }

    useEffect(dependencies = dependencies) {
        cleanup {
            coroutineScope.cancel()
        }
    }

    return coroutineScope
}

fun useLaunchedEffect(
    vararg dependencies: Any?,
    block: suspend () -> Unit
) {
    useEffect(dependencies = dependencies) {
        val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
        coroutineScope.launch {
            block()
        }

        cleanup {
            coroutineScope.cancel()
        }
    }
}
