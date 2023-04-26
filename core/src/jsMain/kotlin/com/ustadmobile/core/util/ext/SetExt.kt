package com.ustadmobile.core.util.ext

actual fun <T> concurrentSafeSetOf(vararg items: T): MutableSet<T> {
    return mutableSetOf(*items)
}
