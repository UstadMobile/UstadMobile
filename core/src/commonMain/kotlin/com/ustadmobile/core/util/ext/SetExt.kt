package com.ustadmobile.core.util.ext

/**
 * Create a concurrent safe mutable set. On Android/JVM this uses ConcurrentHashMap.newKeySet.
 * On Javascript this uses the normal mutableSetOf because Javascript is single threaded anyway.
 */
expect fun <T> concurrentSafeSetOf(vararg items: T): MutableSet<T>
