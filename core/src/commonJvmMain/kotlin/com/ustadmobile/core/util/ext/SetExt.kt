package com.ustadmobile.core.util.ext

import java.util.concurrent.CopyOnWriteArraySet

actual fun <T> concurrentSafeSetOf(vararg items: T): MutableSet<T> {
    return CopyOnWriteArraySet(items.asList())
}
