package com.ustadmobile.libcache.distributed
import com.ustadmobile.door.ext.concurrentSafeMapOf
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.NeighborCache
import com.ustadmobile.libcache.db.entities.NeighborCacheEntry
import com.ustadmobile.libcache.distributed.DistributedCacheConstants.DCACHE_LOGTAG
import com.ustadmobile.libcache.distributed.model.DistributedHashCacheEntry
import com.ustadmobile.libcache.distributed.model.DistributedHashEntries
import com.ustadmobile.libcache.logging.UstadCacheLogger
import com.ustadmobile.xxhashkmp.XXStringHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.Executors

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
    private val xxStringHasher: XXStringHasher,
    private val mtu: Int = DEFAULT_MTU,
): Closeable  {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val executorService = Executors.newCachedThreadPool()

    private val datagramSocket = DatagramSocket()

    val port: Int
        get() = datagramSocket.localPort

    private val discoveredNeighbors = concurrentSafeMapOf<Long, NeighborCache>()

    private val logPrefix = "DistributedCacheHashtable($port)"

    /**
     * Runnable that will send the hashes of everything we have to the neighbor; runs when neighbor
     * is discovered
     */
    inner class SendNeighborHashesRunnable(val neighborCache: NeighborCache): Runnable {
        override fun run() {
            logger.d(DCACHE_LOGTAG,
                "$logPrefix starting new neighbor run for ${neighborCache.neighborIp}:${neighborCache.neighborUdpPort}"
            )

            var urls: List<String>
            var offset = 0
            val entriesPerPacket = DistributedHashEntries.numEntriesFor(mtu)
            val neighborAddress = InetAddress.getByName(neighborCache.neighborIp)

            while(
                cacheDb.cacheEntryDao.getEntryUrlsInOrder(
                    offset = offset, limit  = DATABASE_CHUNK_SIZE
                ).also { urls = it }.isNotEmpty()
            ) {
                logger.d(DCACHE_LOGTAG,
                    "$logPrefix Sending ${urls.size} url hash(es) to " +
                            "${neighborCache.neighborIp}:${neighborCache.neighborUdpPort}"
                )

                urls.chunked(entriesPerPacket).forEach { urlList ->
                    val hashEntries = DistributedHashEntries(
                        httpPort = httpPort,
                        entries = urlList.map {
                            DistributedHashCacheEntry(
                                urlHash = xxStringHasher.hash(it),
                                md5Hi = 0L,
                                md5Lo = 0L,
                            )
                        }
                    )
                    val hashEntryBytes = hashEntries.toBytes()
                    val packet = DatagramPacket(
                        hashEntryBytes, hashEntryBytes.size,
                        neighborAddress, neighborCache.neighborUdpPort
                    )
                    datagramSocket.send(packet)
                }

                offset += DATABASE_CHUNK_SIZE
            }

            logger.d(DCACHE_LOGTAG,
                "$logPrefix finished new neighbor run for ${neighborCache.neighborIp}:${neighborCache.neighborUdpPort}"
            )
        }
    }

    /**
     *
     */
    inner class ReceiveNeighborHashesRunnable: Runnable {

        //Note: maybe this should ensure the neighbor itself is created
        override fun run() {
            logger.d(DCACHE_LOGTAG,"$logPrefix waiting to receive hashes from neighbors")
            val packet = DatagramPacket(ByteArray(mtu), mtu)
            while(!Thread.interrupted()) {
                try {
                    datagramSocket.receive(packet)
                    logger.d(DCACHE_LOGTAG,
                        "$logPrefix received hashes from ${packet.socketAddress}"
                    )

                    val neighborUid = xxStringHasher.neighborUid(packet.address, packet.port)
                    val hashEntries = DistributedHashEntries.fromBytes(packet.data, packet.offset, packet.length)

                    cacheDb.neighborCacheDao.updateHttpPort(neighborUid, packet.port)
                    cacheDb.neighborCacheEntryDao.upsertList(
                        hashEntries.entries.map {
                            NeighborCacheEntry(
                                nceNeighborUid = neighborUid, nceUrlHash = it.urlHash
                            )
                        }
                    )

                    logger.d(DCACHE_LOGTAG,
                        "$logPrefix saved hashes from ${packet.socketAddress} to database"
                    )
                }catch(e: Exception) {
                    logger.e(DCACHE_LOGTAG, "$logPrefix exception reading incoming hashes", e)
                }
            }
        }
    }

    init {
        logger.i(DCACHE_LOGTAG, "$logPrefix initialized on udp port $port")

        //Observe the database for neighbors, then send them our hashes
        scope.launch {
            cacheDb.neighborCacheDao.allNeighborsAsFlow().collect { neighborList ->
                val newNeighbors = neighborList.filter {
                    !discoveredNeighbors.containsKey(it.neighborUid)
                }

                newNeighbors.forEach {
                    logger.d(DCACHE_LOGTAG, "$logPrefix new neighbor ${it.neighborIp}:${it.neighborUdpPort}")
                    discoveredNeighbors[it.neighborUid] = it
                    executorService.submit(SendNeighborHashesRunnable(it))
                }
            }
        }
        executorService.submit(ReceiveNeighborHashesRunnable())
    }

    /**
     * Get a neighbor cache URL to retrieve
     */
    fun neighborUrl(url: String): String? {
        return null
    }


    override fun close() {
        executorService.shutdown()
        scope.cancel()
        datagramSocket.close()
    }

    companion object {

        const val DEFAULT_MTU = 1500

        const val DATABASE_CHUNK_SIZE = 1000
    }
}