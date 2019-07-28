package com.ustadmobile.lib.util

expect fun <K, V> sharedMutableMapOf(vararg pairs: Pair<K, V>): MutableMap<K,V>