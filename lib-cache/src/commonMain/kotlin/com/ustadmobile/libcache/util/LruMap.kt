package com.ustadmobile.libcache.util

import com.ustadmobile.door.util.systemTimeInMillis

class LruMap<K, V>(private val delegate: MutableMap<K, V>): MutableMap<K, V> by delegate {

    private val accessTimeMap: MutableMap<K, Long> = concurrentSafeMapOf()

    override fun put(key: K, value: V): V? {
        accessTimeMap[key] = systemTimeInMillis()
        return delegate.put(key, value)
    }

    override fun putAll(from: Map<out K, V>) {
        from.forEach {
            accessTimeMap[it.key] = systemTimeInMillis()
        }
        return delegate.putAll(from)
    }

    override fun putIfAbsent(key: K, value: V): V? {
        accessTimeMap[key] = systemTimeInMillis()
        return delegate.putIfAbsent(key, value)
    }

    override fun get(key: K): V? {
        accessTimeMap[key] = systemTimeInMillis()
        return delegate.get(key)
    }

}