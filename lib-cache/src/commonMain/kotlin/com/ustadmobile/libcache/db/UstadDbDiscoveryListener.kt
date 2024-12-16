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

    override fun onNeighborDiscovered(neighborUrl: String) {
        scope.launch {
            db.neighborCacheDao.upsertAsync(
                NeighborCache(
                    neighborUid = xxStringHasher.hash(neighborUrl),
                    neighborUrl = neighborUrl,
                    neighborDiscovered = systemTimeInMillis(),
                    neighborPingTime = 0,
                )
            )
        }
    }

    override fun onNeighborLost(neighborUrl: String) {
        scope.launch {
            db.neighborCacheDao.deleteAsync(
                neighborUid = xxStringHasher.hash(neighborUrl),
            )
        }
    }

    fun close() {
        scope.cancel()
    }
}