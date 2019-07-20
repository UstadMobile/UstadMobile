package com.ustadmobile.lib.util

actual fun <K, V> sharedMutableMapOf(vararg pairs: Pair<K, V>) = mutableMapOf(*pairs)
