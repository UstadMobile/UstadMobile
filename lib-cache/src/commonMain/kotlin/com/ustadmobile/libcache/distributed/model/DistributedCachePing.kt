package com.ustadmobile.libcache.distributed.model

import java.nio.ByteBuffer

data class DistributedCachePing(
    override val id: Int,
    override val payload: ByteArray,
): DistributedCachePacket(), DistributedCacheWhatWithIdAndPayload {

    override fun toBytes() = this.toBytesArray(WHAT_PING)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DistributedCachePing) return false

        if (id != other.id) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }

    companion object {
        fun ByteBuffer.readDistributedCachePing(): DistributedCachePing {
            val (id, payload) = readIdAndPayload()
            return DistributedCachePing(id, payload)
        }
    }
}