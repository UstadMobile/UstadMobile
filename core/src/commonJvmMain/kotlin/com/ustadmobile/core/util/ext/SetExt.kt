package com.ustadmobile.core.util.ext

import java.util.concurrent.ConcurrentHashMap

actual fun <T> concurrentSafeSetOf(vararg items: T): MutableSet<T> {

    val mutableSet: MutableSet<T> = ConcurrentHashMap.newKeySet()
    mutableSet.addAll(items)

    return mutableSet
}
