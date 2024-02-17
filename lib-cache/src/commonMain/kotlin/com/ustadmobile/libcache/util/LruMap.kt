package com.ustadmobile.libcache.util

import com.ustadmobile.door.util.systemTimeInMillis
import java.util.function.BiFunction

/**
 * Basic implementation of a Least Recently Used map to run a memory cache
 */
class LruMap<K, V>(
    private val delegate: MutableMap<K, V>,
    private val maxItems: Int = 5_000,
): MutableMap<K, V> by delegate {

    private val accessTimeMap: MutableMap<K, Long> = concurrentSafeMapOf()

    private fun trimIfNeeded() {
        if(size > maxItems) {
            val numItemsToDelete = maxItems - size

            val itemsToDelete = accessTimeMap.entries.sortedBy {
                it.value
            }.subList(0, numItemsToDelete)

            itemsToDelete.forEach {
                remove(it.key)
                accessTimeMap.remove(it.key)
            }
        }
    }

    override fun put(key: K, value: V): V? {
        accessTimeMap[key] = systemTimeInMillis()
        return delegate.put(key, value).also { trimIfNeeded() }
    }

    override fun putAll(from: Map<out K, V>) {
        from.forEach {
            accessTimeMap[it.key] = systemTimeInMillis()
        }
        return delegate.putAll(from).also { trimIfNeeded() }
    }

    override fun putIfAbsent(key: K, value: V): V? {
        accessTimeMap[key] = systemTimeInMillis()
        return delegate.putIfAbsent(key, value).also { trimIfNeeded() }
    }

    override fun get(key: K): V? {
        accessTimeMap[key] = systemTimeInMillis()
        return delegate.get(key).also { trimIfNeeded() }
    }

    override fun compute(key: K, remappingFunction: BiFunction<in K, in V?, out V?>): V? {
        return delegate.compute(key, remappingFunction).also { trimIfNeeded() }
    }
}