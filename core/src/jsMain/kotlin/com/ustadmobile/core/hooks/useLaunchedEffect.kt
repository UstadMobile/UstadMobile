package com.ustadmobile.core.hooks

import kotlinx.coroutines.*
import react.StateInstance
import react.useEffect
import react.useState

/**
 * Launch a Coroutine function as an effect. Returns a StateInstance that can be used
 * to access the result.
 */
fun <R> useLaunchedEffect(
    vararg dependencies: Any?,
    block: suspend () -> R,
) : StateInstance<R?> {
    val state: StateInstance<R?> = useState()
    useEffect(dependencies = dependencies) {
        val scope = CoroutineScope(Dispatchers.Main + Job())
        scope.launch {
            state.component2().invoke(block())
        }

        cleanup {
            scope.cancel()
        }
    }

    return state
}
