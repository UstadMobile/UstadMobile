package com.ustadmobile.libcache.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param neighborUid the xxhash of neighborUrl
 * @param neighborUrl the discovered url of the neighbor (e.g. http://ip.addr:port/ )
 * @param neighborDiscovered the time the neighbor was discovered
 */
@Entity
data class NeighborCache(
    @PrimaryKey
    var neighborUid: Long = 0L,
    var neighborUrl: String = "",
    var neighborDiscovered: Long = 0L,
    var neighborPingTime: Int = 0,
)
