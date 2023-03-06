package com.ustadmobile.core.util.ext

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

suspend fun <T> MutableStateFlow<T>.whenSubscribed(
    block: suspend () -> Unit,
) {
    subscriptionCount.map { it > 0 }.distinctUntilChanged().collectLatest { active ->
        if(active) {
            block()
        }
    }
}
