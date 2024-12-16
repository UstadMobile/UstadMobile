package com.ustadmobile.libcache.distributed

interface DistributedCacheNeighborDiscoveryListener {

    fun onNeighborDiscovered(neighborUrl: String)

    fun onNeighborLost(neighborUrl: String)

}