package com.ustadmobile.lib.util

import java.util.concurrent.ConcurrentHashMap

actual fun <K, V> sharedMutableMapOf(vararg pairs: Pair<K, V>): MutableMap<K,V> {
    val map = ConcurrentHashMap<K,V>(pairs.size)
    map.putAll(pairs)
    return map
}
