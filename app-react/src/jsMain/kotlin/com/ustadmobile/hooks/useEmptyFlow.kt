package com.ustadmobile.hooks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import react.useMemo

fun <T: Any> useEmptyFlow(): Flow<T> = useMemo(dependencies = emptyArray()) {
    emptyFlow()
}
