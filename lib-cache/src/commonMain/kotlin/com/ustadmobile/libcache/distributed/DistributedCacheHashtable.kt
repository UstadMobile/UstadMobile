package com.ustadmobile.libcache.distributed
import com.ustadmobile.libcache.db.UstadCacheDb

/**
 * Monitor newly discovered neighbors (just observe flow).
 *
 * 
 */
class DistributedCacheHashtable(
    private val cacheDb: UstadCacheDb,
) : DistributedCacheNeighborDiscoveryListener {

    /**
     * Get a neighbor cache URL to retrieve
     */
    fun neighborUrl(url: String): String? {
        return null
    }

    override fun onNeighborDiscovered(neighborUrl: String) {
        //update database
    }

    override fun onNeighborLost(neighborUrl: String) {

    }

    fun close() {

    }
}