package com.ustadmobile.libcache.distributed
import com.ustadmobile.door.ext.concurrentSafeMapOf
import com.ustadmobile.door.ext.withDoorTransaction
import com.ustadmobile.door.room.InvalidationTrackerObserver
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.NeighborCache
import com.ustadmobile.libcache.db.entities.NeighborCacheEntry
import com.ustadmobile.libcache.distributed.DistributedCacheConstants.DCACHE_LOGTAG
import com.ustadmobile.libcache.distributed.model.DistributedCachePacket
import com.ustadmobile.libcache.distributed.model.DistributedCachePing
import com.ustadmobile.libcache.distributed.model.DistributedCachePong
import com.ustadmobile.libcache.distributed.model.DistributedHashCacheEntry
import com.ustadmobile.libcache.distributed.model.DistributedHashEntries
import com.ustadmobile.libcache.logging.UstadCacheLogger
import com.ustadmobile.xxhashkmp.XXStringHasher
import kotlinx.atomicfu.atomic
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
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.max

/**
 * Monitor newly discovered neighbors (just observe flow). When a new node is found, send
 * DistributedHashEntries to it so it knows everything we have.
 *
 * This operates a UDP DatagramSocket which is used to send a list of available entries to neighbors
 * and receive entries from neighbors
 *
 * @param cacheDb the cache database we will observe to watch for new neighbors
 * @param httpPort the EmbeddedServer http port that neighbors can use to retrieve entries
 * @param mtu MTU for UDP packets: used when sending hash entries to packetize
 * @parma pingInterval the interval in milliseconds between pings to neighbors
 * @param neighborLostThreshold the number of milliseconds after which a neighbor is considered lost
 * @param deviceName function to provide the device name as it will be shown to other devices
 */
class DistributedCacheHashtable(
    private val cacheDb: UstadCacheDb,
    private val httpPort: Int,
    private val logger: UstadCacheLogger,
    private val xxStringHasher: XXStringHasher,
    private val mtu: Int = DEFAULT_MTU,
    pingInterval: Long = DEFAULT_PING_INTERVAL,
    private val neighborLostThreshold: Long = DEFAULT_NEIGHBOR_LOST_THRESHOLD,
    private val deviceName: () -> String,
): Closeable  {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val executorService = Executors.newScheduledThreadPool(2)

    private val datagramSocket = DatagramSocket()

    val port: Int
        get() = datagramSocket.localPort

    private val discoveredNeighbors = concurrentSafeMapOf<Long, NeighborCache>()

    private val logPrefix = "DistributedCacheHashtable($port ${deviceName()})"

    data class PendingPing(
        val id: Int,
        val timeSent: Long,
        val remoteAddress: InetAddress,
    )

    private val pendingPings: MutableMap<Int, PendingPing> = concurrentSafeMapOf()

    private val pingIdAtomic = atomic(0)

    private val sendLock = ReentrantLock()

    private fun DatagramSocket.sendDistributedHashEntries(
        urls: List<String>,
        neighborCache: NeighborCache,
        neighborAddress: InetAddress = InetAddress.getByName(neighborCache.neighborIp)
    ) {
        logger.d(DCACHE_LOGTAG,
            "$logPrefix Sending ${urls.size} url hash(es) to " +
                    "${neighborCache.neighborIp}:${neighborCache.neighborUdpPort}"
        )

        val entriesPerPacket = DistributedHashEntries.numEntriesFor(mtu)

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
            send(packet)
        }
    }

    /**
     * Runnable that will send the hashes of everything we have to the neighbor; runs when neighbor
     * is discovered
     */
    inner class SendNeighborHashesRunnable(private val neighborCache: NeighborCache): Runnable {
        override fun run() {
            logger.d(DCACHE_LOGTAG,
                "$logPrefix starting new neighbor run for ${neighborCache.neighborIp}:${neighborCache.neighborUdpPort}"
            )

            var urls: List<String>
            var offset = 0

            val neighborAddress = InetAddress.getByName(neighborCache.neighborIp)

            while(
                cacheDb.cacheEntryDao.getEntryUrlsInOrder(
                    offset = offset, limit  = DATABASE_CHUNK_SIZE
                ).also { urls = it }.isNotEmpty()
            ) {
                datagramSocket.sendDistributedHashEntries(urls, neighborCache, neighborAddress)

                offset += DATABASE_CHUNK_SIZE
            }

            logger.d(DCACHE_LOGTAG,
                "$logPrefix finished new neighbor run for ${neighborCache.neighborIp}:${neighborCache.neighborUdpPort}"
            )
        }
    }

    /**
     * Receive packets from the datagram socket and action them:
     *  a) List of hashes from neighbor
     *  b) Ping from neighbor
     *  c) Pong reply to ping sent by this node
     */
    inner class ReceivePacketsRunnable: Runnable {
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
                    val dCachePacket = DistributedCachePacket.fromBytes(packet.data, packet.offset, packet.length)

                    cacheDb.neighborCacheDao.updateHttpPort(neighborUid, packet.port)

                    when(dCachePacket) {
                        is DistributedHashEntries -> {
                            cacheDb.neighborCacheEntryDao.upsertList(
                                dCachePacket.entries.map {
                                    NeighborCacheEntry(
                                        nceNeighborUid = neighborUid, nceUrlHash = it.urlHash
                                    )
                                }
                            )
                            logger.d(DCACHE_LOGTAG,
                                "$logPrefix saved hashes from ${packet.socketAddress} to database"
                            )
                        }

                        is DistributedCachePing -> {
                            val pongReply = DistributedCachePong(dCachePacket.id, dCachePacket.payload)
                            val replyBytes = pongReply.toBytes()
                            val replyPacket = DatagramPacket(
                                replyBytes, replyBytes.size, packet.address, packet.port
                            )
                            sendLock.withLock {
                                datagramSocket.send(replyPacket)
                            }
                            logger.d(DCACHE_LOGTAG, "$logPrefix sent pong reply to ${packet.socketAddress}")
                        }

                        is DistributedCachePong -> {
                            val pendingPing = pendingPings.remove(dCachePacket.id)
                            if(pendingPing != null) {
                                val pingTime = max(systemTimeInMillis() - pendingPing.timeSent, 1L)
                                val updates = cacheDb.neighborCacheDao.updatePingTime(
                                    neighborUid = xxStringHasher.neighborUid(packet.address, packet.port),
                                    pingTime = pingTime.toInt(),
                                    timeNow = systemTimeInMillis()
                                )

                                logger.d(DCACHE_LOGTAG,
                                    "$logPrefix ping time to ${packet.socketAddress} is ${pingTime}ms updates=$updates"
                                )
                            }else {
                                logger.d(DCACHE_LOGTAG, "Could not find pending ping for id ${dCachePacket.id}")
                            }
                        }
                    }
                }catch(e: Exception) {
                    logger.e(DCACHE_LOGTAG, "$logPrefix exception reading incoming packet", e)
                }
            }
        }
    }

    inner class SendNewCacheEntriesRunnable: Runnable {
        override fun run() {
            logger.d(DCACHE_LOGTAG, "$logPrefix SendNewCacheEntriesRunnable: Looking for new cache entries to send out")
            val (newEntries, allNodes) = cacheDb.withDoorTransaction {
                val entries = cacheDb.newCacheEntryDao.findAllNewEntries()
                val nodes = cacheDb.neighborCacheDao.allNeighbors()
                cacheDb.newCacheEntryDao.clearAll()

                Pair(entries, nodes)
            }

            logger.d(DCACHE_LOGTAG,
                "SendNewCacheEntriesRunnable: sending ${newEntries.size} new entry hashes" +
                    " to ${allNodes.size} nodes "
            )

            allNodes.forEach { neighbor ->
                datagramSocket.sendDistributedHashEntries(
                    urls = newEntries.map { it.nceUrl },
                    neighborCache = neighbor,
                )
            }
        }
    }

    inner class SendPingsRunnable: Runnable {
        override fun run() {
            val allNodes = cacheDb.neighborCacheDao.allNeighbors()
            logger.d(DCACHE_LOGTAG, "$logPrefix: sending pings to ${allNodes.size} nodes")
            val deviceNameVal = deviceName()

            allNodes.forEach { neighbor ->
                try {
                    val address = InetAddress.getByName(neighbor.neighborIp)
                    val ping = DistributedCachePing(
                        id = pingIdAtomic.incrementAndGet(),
                        deviceName = deviceNameVal,
                        payload = ByteArray(0)
                    )
                    pendingPings[ping.id] = PendingPing(ping.id, systemTimeInMillis(), address)
                    val pingPacketBytes = ping.toBytes()
                    sendLock.withLock {
                        datagramSocket.send(
                            DatagramPacket(pingPacketBytes, pingPacketBytes.size, address, neighbor.neighborUdpPort)
                        )
                    }

                    logger.d(DCACHE_LOGTAG,
                        "$logPrefix: send ping to ${address.hostAddress}:${neighbor.neighborUdpPort}"
                    )
                }catch(e: Throwable) {
                    logger.e(DCACHE_LOGTAG, "$logPrefix exception sending ping to $neighbor", e)
                }
            }

            cacheDb.neighborCacheDao.updateStatuses(
                timeNow = systemTimeInMillis(),
                lostThreshold = neighborLostThreshold
            )
        }
    }


    private val newCacheEntryInvalidationCallback = object: InvalidationTrackerObserver(arrayOf("NewCacheEntry")) {

        override fun onInvalidated(tables: Set<String>) {
            executorService.submit(SendNewCacheEntriesRunnable())
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

        cacheDb.invalidationTracker.addObserver(newCacheEntryInvalidationCallback)
        executorService.submit(ReceivePacketsRunnable())
        executorService.scheduleWithFixedDelay(
            SendPingsRunnable(), pingInterval, pingInterval, TimeUnit.MILLISECONDS
        )
    }

    /**
     * Get a neighbor cache URL to retrieve
     */
    @Suppress("unused")
    fun neighborUrl(
        @Suppress("UNUSED_PARAMETER") url: String
    ): String? {
        return null
    }


    override fun close() {
        cacheDb.invalidationTracker.removeObserver(newCacheEntryInvalidationCallback)
        executorService.shutdown()
        scope.cancel()
        datagramSocket.close()
    }

    companion object {

        const val DEFAULT_MTU = 1500

        const val DATABASE_CHUNK_SIZE = 1000

        const val DEFAULT_PING_INTERVAL = 3_000L

        const val DEFAULT_NEIGHBOR_LOST_THRESHOLD = 10_000L
    }
}