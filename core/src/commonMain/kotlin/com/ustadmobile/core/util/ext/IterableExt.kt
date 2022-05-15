package com.ustadmobile.core.util.ext

/**
 * Given a Iterable of longs, combined them all together with a binary or
 */
fun Iterable<Long>.foldWithBinaryOr(): Long {
    return fold(0L) { acc, newVal ->
        acc or newVal
    }
}
