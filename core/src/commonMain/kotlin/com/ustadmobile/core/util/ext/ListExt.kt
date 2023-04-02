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

