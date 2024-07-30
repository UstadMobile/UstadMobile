package com.ustadmobile.core.util.ext

import dev.icerock.moko.resources.StringResource

/**
 * "Upsert" for a list, commonly required for edit scenarios where an element has been returned
 * that might be updating something already in the list, or it might be a new item.
 */
fun <T> List<T>.replaceOrAppend(
    element: T,
    replacePredicate: (T) -> Boolean,
): List<T> {
    val replaceIndex = indexOfFirst(replacePredicate)
    return if(replaceIndex >= 0) {
        toMutableList().also {
            it[replaceIndex] = element
        }.toList()
    } else {
        (this + element)
    }
}

fun <T> List<T>.replace(
    element: T,
    replacePredicate: (T) -> Boolean,
): List<T> {
    val replaceIndex = indexOfFirst(replacePredicate)
    if(replaceIndex == -1)
        throw IllegalArgumentException("element to replace not found")

    return toMutableList().also {
        it[replaceIndex] = element
    }.toList()
}

/**
 * Shorthand to efficiently find a list of keys that were in a list before, but are not in
 * the new list. This is commonly used in one-many join scenarios where we need to see which
 * keys need deactivated
 */
fun <T, R> List<T>.findKeysNotInOtherList(
    otherList: List<T>,
    key: (T) -> R,
): List<R> {
    val otherListKeys = otherList.map(key)
    val thisListKeys = map(key)
    return thisListKeys.filter {
        it !in otherListKeys
    }
}

/**
 * Trim the given
 */
fun <T> List<T>.trimToSize(maxSize: Int) : List<T> {
    return if(size > maxSize) {
        subList(0, maxSize)
    }else {
        this
    }
}

/**
 * If the list is smaller than the given size, pad it to the given size by adding items to the end
 */
inline fun <T> List<T>.padEnd(
    minSize: Int,
    item: (index: Int) -> T
): List<T> {
    return if(size < minSize) {
        this + (size until minSize).map(item)
    }else {
        this
    }
}

/**
 * Get the last distinct item in a list by a given key. This could be useful when we have a list of
 * updates, but only want to apply the latest received item.
 */
inline fun <T, K> List<T>.lastDistinctBy(
    selector: (T) -> K
): List<T>{
    val map = mutableMapOf<K, T>()
    forEach {
        map[selector(it)] = it
    }
    return map.values.toList()
}


fun List<Pair<StringResource, Long>>.filterByFlags(value: Long) : List<Pair<StringResource, Long>> {
    return filter { value.hasFlag(it.second) }
}

fun <T> List<T>.duplicates() : List<T> {
    return filter { item ->
        count { it == item }  > 1
    }
}

fun <T> List<T>?.toEmptyIfNull(): List<T> = this ?: emptyList()
