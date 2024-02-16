package com.ustadmobile.libcache

/**
 * This function provides the paths where a given entry will initially be stored
 */
fun interface CachePathsProvider {

    operator fun invoke(entryToStore: CacheEntryToStore): CachePaths

}