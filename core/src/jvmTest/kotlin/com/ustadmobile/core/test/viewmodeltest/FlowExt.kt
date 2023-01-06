package com.ustadmobile.core.test.viewmodeltest

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlin.time.Duration

suspend fun <T> Flow<T>.awaitMatch(
    timeout: Duration? = null,
    filterBlock: (T) -> Boolean
) {
    filter(filterBlock).test(timeout = timeout) {
        awaitItem()
    }
}
