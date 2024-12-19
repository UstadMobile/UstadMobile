package com.ustadmobile.libcache.distributed

interface DistributedCacheNeighborDiscoveryListener {

    fun onNeighborDiscovered(neighborIp: String, neighborUdpPort: Int)

    fun onNeighborLost(neighborIp: String, neighborUdpPort: Int)

}