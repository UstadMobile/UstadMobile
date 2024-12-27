package com.ustadmobile.libcache.distributed.model

import com.ustadmobile.libcache.util.readPayload
import com.ustadmobile.libcache.util.readShortString
import com.ustadmobile.libcache.util.writePayload
import com.ustadmobile.libcache.util.writeShortString
import java.nio.ByteBuffer

data class DistributedCachePing(
    override val id: Int,
    val deviceName: String,
    override val payload: ByteArray,
): DistributedCachePacket(), DistributedCacheWhatWithIdAndPayload {

    override fun toBytes(): ByteArray {
        val size = OVERHEAD_SIZE + payload.size + deviceName.toByteArray().size
        val byteBuffer = ByteBuffer.allocate(size)
        byteBuffer.put(WHAT_PING)
        byteBuffer.putInt(id)
        byteBuffer.writeShortString(deviceName)
        byteBuffer.writePayload(payload)

        return byteBuffer.array()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DistributedCachePing) return false

        if (id != other.id) return false
        if (deviceName != other.deviceName) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + deviceName.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }

    companion object {

        //What byte, id, device name length, payload length
        const val OVERHEAD_SIZE = 1 + 4 + 1 + 2

        fun ByteBuffer.readDistributedCachePing(): DistributedCachePing {
            val id = getInt()
            val deviceName = readShortString()
            val payload = readPayload()

            return DistributedCachePing(id, deviceName, payload)
        }
    }
}