package com.ustadmobile.core.util.ext

/**
 * If the given item is in the set, then remove it. If the item is not in the set, add it.
 */
fun <T> Set<T>.toggle(item: T) : Set<T> {
    return if(item in this) {
        filter { it != item }.toSet()
    }else {
        toMutableSet().apply {
            add(item)
        }.toSet()
    }
}