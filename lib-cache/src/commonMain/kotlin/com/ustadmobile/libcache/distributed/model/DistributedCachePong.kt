package com.ustadmobile.libcache.distributed.model

import java.nio.ByteBuffer

class DistributedCachePong(
    override val id: Int,
    override val payload: ByteArray,
) : DistributedCachePacket(), DistributedCacheWhatWithIdAndPayload {

    override fun toBytes() = this.toBytesArray(WHAT_PONG)

    companion object {
        fun ByteBuffer.readDistributedCachePong(): DistributedCachePong {
            val (id, payload) = readIdAndPayload()
            return DistributedCachePong(id, payload)
        }

    }

}