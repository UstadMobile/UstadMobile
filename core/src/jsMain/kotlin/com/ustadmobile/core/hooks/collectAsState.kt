package com.ustadmobile.core.hooks

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import react.StateInstance
import react.useEffect
import react.useState

/**
 * Collect the receiver flow as a state.
 */
fun <T> Flow<T>.collectAsState(
    initialState: T,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
): StateInstance<T> {
    val state = useState { initialState }

    useEffect(dependencies = arrayOf(this, dispatcher)) {
        val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

        coroutineScope.launch(dispatcher) {
            this@collectAsState.collect {
                state.component2().invoke(it)
            }
        }

        cleanup {
            coroutineScope.cancel()
        }
    }

    return state
}
