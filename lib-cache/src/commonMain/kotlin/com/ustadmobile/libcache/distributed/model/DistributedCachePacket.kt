package com.ustadmobile.libcache.distributed.model

import com.ustadmobile.libcache.distributed.model.DistributedCachePing.Companion.readDistributedCachePing
import com.ustadmobile.libcache.distributed.model.DistributedCachePong.Companion.readDistributedCachePong
import com.ustadmobile.libcache.distributed.model.DistributedHashEntries.Companion.readDistributedHashEntries
import java.nio.ByteBuffer

sealed class DistributedCachePacket {

    abstract val httpPort: Int

    abstract fun toBytes(): ByteArray

    companion object {

        //What + http port
        const val DCACHE_PACKET_OVERHEAD = 1 + 4

        fun fromBytes(
            bytesArray: ByteArray,
            offset: Int = 0,
            len: Int = bytesArray.size
        ): DistributedCachePacket {
            val buffer = ByteBuffer.wrap(bytesArray, offset, len)
            val what = buffer.get()
            val httpPort = buffer.getInt()
            return when(what) {
                WHAT_ENTRIES -> buffer.readDistributedHashEntries(httpPort)
                WHAT_PING -> buffer.readDistributedCachePing(httpPort)
                WHAT_PONG -> buffer.readDistributedCachePong(httpPort)
                else -> throw IllegalArgumentException("DistributedCachePacket.fromBytes: WHAT byte not recognized")
            }
        }


        const val WHAT_ENTRIES = 1.toByte()

        const val WHAT_PING = 2.toByte()

        const val WHAT_PONG = 3.toByte()

    }
}
