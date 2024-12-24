package com.ustadmobile.libcache.distributed.model

import com.ustadmobile.libcache.distributed.model.DistributedCachePing.Companion.readDistributedCachePing
import com.ustadmobile.libcache.distributed.model.DistributedHashEntries.Companion.readDistributedHashEntries
import java.nio.ByteBuffer

sealed class DistributedCachePacket {

    abstract fun toBytes(): ByteArray

    companion object {

        fun fromBytes(
            bytesArray: ByteArray,
            offset: Int = 0,
            len: Int = bytesArray.size
        ): DistributedCachePacket {
            val buffer = ByteBuffer.wrap(bytesArray, offset, len)
            val what = buffer.get()
            return when(what) {
                WHAT_ENTRIES -> buffer.readDistributedHashEntries()
                WHAT_PING -> buffer.readDistributedCachePing()
                else -> throw IllegalArgumentException("DistributedCachePacket.fromBytes: WHAT byte not recognized")
            }
        }


        const val WHAT_ENTRIES = 1.toByte()

        const val WHAT_PING = 2.toByte()

        const val WHAT_PONG = 3.toByte()

    }
}
