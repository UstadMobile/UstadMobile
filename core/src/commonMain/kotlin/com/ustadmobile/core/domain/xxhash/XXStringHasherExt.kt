package com.ustadmobile.core.domain.xxhash

/**
 * Where the given String is a valid long, returns the Long value (e.g. toLong). Otherwise, uses
 * the hasher (receiver) to hash the string.
 *
 * This is the strategy that is used to generate uid 64bit longs for OneRoster and xAPI items.
 */
fun XXStringHasher.toLongOrHash(string: String): Long {
    return string.toLongOrNull() ?: hash(string)
}
