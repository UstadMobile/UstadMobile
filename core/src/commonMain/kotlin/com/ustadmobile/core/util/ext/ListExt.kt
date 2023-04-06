package com.ustadmobile.core.util.ext

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

/**
 * Shorthand to efficiently find a list of keys that were in a list before, but are not in
 * the new list. This is commonly used in one-many join scenarios where we need to see which
 * keys need deactivated
 */
fun <T, R> List<T>.filterKeysNotInOtherList(
    otherList: List<T>,
    key: (T) -> R,
): List<R> {
    val otherListKeys = otherList.map(key)
    val thisListKeys = map(key)
    return thisListKeys.filter {
        it !in otherListKeys
    }
}


