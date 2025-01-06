package com.ustadmobile.libcache.distributed.model

import com.ustadmobile.libcache.util.readPayload
import com.ustadmobile.libcache.util.writePayload
import java.nio.ByteBuffer

class DistributedCachePong(
    override val id: Int,
    override val httpPort: Int,
    override val payload: ByteArray,
) : DistributedCachePacket(), DistributedCacheWhatWithIdAndPayload {

    override fun toBytes(): ByteArray {
        val byteBuf = ByteBuffer.allocate(OVERHEAD_SIZE + payload.size)
        byteBuf.put(WHAT_PONG)
        byteBuf.putInt(httpPort)
        byteBuf.putInt(id)
        byteBuf.writePayload(payload)
        return byteBuf.array()
    }

    companion object {

        //What byte, httpPort + id integer + payload length (short)
        const val OVERHEAD_SIZE = DCACHE_PACKET_OVERHEAD + 4 + 2

        fun ByteBuffer.readDistributedCachePong(httpPort: Int): DistributedCachePong {
            val id = getInt()
            val payload = readPayload()
            return DistributedCachePong(id, httpPort, payload)
        }

    }

}