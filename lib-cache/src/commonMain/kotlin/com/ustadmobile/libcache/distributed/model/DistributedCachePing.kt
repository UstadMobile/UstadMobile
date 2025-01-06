package com.ustadmobile.libcache.distributed.model

import com.ustadmobile.libcache.util.readPayload
import com.ustadmobile.libcache.util.readShortString
import com.ustadmobile.libcache.util.writePayload
import com.ustadmobile.libcache.util.writeShortString
import java.nio.ByteBuffer

data class DistributedCachePing(
    override val id: Int,
    override val httpPort: Int,
    val deviceName: String,
    override val payload: ByteArray,
): DistributedCachePacket(), DistributedCacheWhatWithIdAndPayload {

    override fun toBytes(): ByteArray {
        val size = OVERHEAD_SIZE + payload.size + deviceName.toByteArray().size
        val byteBuffer = ByteBuffer.allocate(size)
        byteBuffer.put(WHAT_PING)
        byteBuffer.putInt(httpPort)
        byteBuffer.putInt(id)
        byteBuffer.writeShortString(deviceName)
        byteBuffer.writePayload(payload)

        return byteBuffer.array()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DistributedCachePing) return false

        if (id != other.id) return false
        if (httpPort != other.httpPort) return false
        if (deviceName != other.deviceName) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + httpPort
        result = 31 * result + deviceName.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }

    companion object {

        //What (byte, httpPort), id, device name length, payload length
        const val OVERHEAD_SIZE = DCACHE_PACKET_OVERHEAD + 4 + 2 + 2

        fun ByteBuffer.readDistributedCachePing(httpPort: Int): DistributedCachePing {
            val id = getInt()
            val deviceName = readShortString()
            val payload = readPayload()

            return DistributedCachePing(id, httpPort, deviceName, payload)
        }
    }
}