package com.ustadmobile.libcache.util

import java.util.concurrent.ConcurrentHashMap

actual fun <K, V> concurrentSafeMapOf(vararg pairs : Pair<K, V>): MutableMap<K, V> {
    return ConcurrentHashMap<K, V>().also {
        it.putAll(pairs)
    }
}
