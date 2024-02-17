package com.ustadmobile.libcache.util

expect fun <K, V> concurrentSafeMapOf(vararg pairs : Pair<K, V>): MutableMap<K, V>
