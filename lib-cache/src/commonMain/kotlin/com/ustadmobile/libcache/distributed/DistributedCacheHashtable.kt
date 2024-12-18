package com.ustadmobile.libcache.distributed
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.distributed.DistributedCacheConstants.DCACHE_LOGTAG
import com.ustadmobile.libcache.logging.UstadCacheLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.DatagramSocket

/**
 * Monitor newly discovered neighbors (just observe flow). When a new node is found, send
 * DistributedHashEntries to it so it knows everything we have.
 *
 * This operates a UDP DatagramSocket which is used to send a list of available entries to neighbors
 * and receive entries from neighbors
 *
 * @param cacheDb the cache database we will observe to watch for new neighbors
 * @param httpPort the EmbeddedServer http port that neighbors can use to retrieve entries
 */
class DistributedCacheHashtable(
    private val cacheDb: UstadCacheDb,
    private val httpPort: Int,
    private val logger: UstadCacheLogger,
)  {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val datagramSocket = DatagramSocket()

    val port: Int
        get() = datagramSocket.localPort

    init {
        logger.i(DCACHE_LOGTAG, "DistributedCacheHashtable: init on port $port")
        scope.launch {
            //Observe the database for neighbors, then send them our hashes


        }
    }

    /**
     * Get a neighbor cache URL to retrieve
     */
    fun neighborUrl(url: String): String? {
        return null
    }


    fun close() {
        scope.cancel()
    }
}