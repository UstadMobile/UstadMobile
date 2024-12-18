package com.ustadmobile.libcache.db

import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libcache.db.entities.NeighborCache
import com.ustadmobile.libcache.distributed.DistributedCacheNeighborDiscoveryListener
import com.ustadmobile.xxhashkmp.XXStringHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class UstadDbDiscoveryListener(
    private val db: UstadCacheDb,
    private val scope: CoroutineScope,
    private val xxStringHasher: XXStringHasher,
): DistributedCacheNeighborDiscoveryListener {

    override fun onNeighborDiscovered(neighborIp: String, neighborUdpPort: Int) {
        scope.launch {
            db.neighborCacheDao.upsertAsync(
                NeighborCache(
                    neighborUid = xxStringHasher.hash("$neighborIp:$neighborUdpPort"),
                    neighborIp = neighborIp,
                    neighborDiscovered = systemTimeInMillis(),
                    neighborPingTime = 0,
                )
            )
        }
    }

    override fun onNeighborLost(neighborIp: String, neighborUdpPort: Int) {
        scope.launch {
            db.neighborCacheDao.deleteAsync(
                neighborUid = xxStringHasher.hash("$neighborIp:$neighborUdpPort"),
            )
        }
    }

    fun close() {
        scope.cancel()
    }
}