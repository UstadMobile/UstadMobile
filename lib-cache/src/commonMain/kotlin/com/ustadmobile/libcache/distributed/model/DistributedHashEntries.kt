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
    val httpPort: Int,
    val entries: List<DistributedHashEntry>
) {

    val size: Int
        //1 byte version, 4 bytes port, 4 bytes for number of entries, and then for each entry
        get() = 1 + 4 + 4 + (entries.size * DistributedHashEntry.SIZE)

    fun toBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(size)
        buffer.put(version)
        buffer.putInt(httpPort)
        buffer.putInt(entries.size)
        entries.forEach { it.writeBytes(buffer) }
        return buffer.array()
    }

    companion object {

        fun fromBytes(
            bytesArray: ByteArray,
            offset: Int = 0,
            len: Int = bytesArray.size
        ) : DistributedHashEntries {
            val buffer = ByteBuffer.wrap(bytesArray, offset, len)

            val version = buffer.get()
            val httpPort = buffer.int
            val numEntries = buffer.int
            val entriesList = mutableListOf<DistributedHashEntry>()
            for(i in 0 until numEntries) {
                entriesList.add(DistributedHashEntry.readBytes(buffer))
            }

            return DistributedHashEntries(version, httpPort, entriesList)
        }
    }
}