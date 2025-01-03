package com.ustadmobile.libcache.distributed.model

import java.nio.ByteBuffer

/**
 * Holder that will be used to exchange info on available cache entries e.g. One instance of
 * DistributedHashEntries is serialized to a byte array and sent using a UDP DatagramPacket.
 *
 * @param version
 * @param httpPort The port number for the http server on which these entries are available e.g. the
 *        port for EmbeddedHttpServer.
 * @param entries list of entries
 */
data class DistributedHashEntries(
    val version: Byte = 1,
    override val httpPort: Int,
    val entries: List<DistributedHashCacheEntry>
): DistributedCachePacket() {

    val size: Int
        get() = OVERHEAD_SIZE + (entries.size * DistributedHashCacheEntry.SIZE)

    override fun toBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(size)
        buffer.put(WHAT_ENTRIES)
        buffer.putInt(httpPort)
        buffer.put(version)
        buffer.putInt(entries.size)
        entries.forEach { it.writeBytes(buffer) }
        return buffer.array()
    }

    companion object {

        //(1 byte WHAT, 4 byte httpPort), 1 byte version, 4 bytes for number of entries, and then for each entry
        const val OVERHEAD_SIZE = DCACHE_PACKET_OVERHEAD + 1 + 4

        fun numEntriesFor(mtu: Int): Int {
            return (mtu - OVERHEAD_SIZE) / DistributedHashCacheEntry.SIZE
        }

        fun ByteBuffer.readDistributedHashEntries(httpPort: Int): DistributedHashEntries {
            val version = get()
            val numEntries = int
            val entriesList = mutableListOf<DistributedHashCacheEntry>()
            for(i in 0 until numEntries) {
                entriesList.add(DistributedHashCacheEntry.readBytes(this))
            }

            return DistributedHashEntries(version, httpPort, entriesList)
        }
    }
}