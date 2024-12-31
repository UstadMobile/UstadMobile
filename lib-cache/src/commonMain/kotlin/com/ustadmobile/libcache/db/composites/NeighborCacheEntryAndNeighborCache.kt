package com.ustadmobile.libcache.db.composites

import androidx.room.Embedded
import com.ustadmobile.libcache.db.entities.NeighborCache
import com.ustadmobile.libcache.db.entities.NeighborCacheEntry

data class NeighborCacheEntryAndNeighborCache(
    @Embedded
    var neighborCache: NeighborCache = NeighborCache(),
    @Embedded
    var neighborCacheEntry: NeighborCacheEntry = NeighborCacheEntry()
)
