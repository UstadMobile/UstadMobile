package com.ustadmobile.libcache.distributed.model

import java.nio.ByteBuffer

data class DistributedHashEntry(
    val urlHash: Long,
    val md5Hi: Long,
    val md5Lo: Long,
) {

    fun writeBytes(buffer: ByteBuffer) {
        buffer.putLong(urlHash)
        buffer.putLong(md5Hi)
        buffer.putLong(md5Lo)
    }

    companion object {

        /**
         * Size of a DistributedHashEntry in bytes
         */
        const val SIZE = 24

        fun readBytes(buffer: ByteBuffer): DistributedHashEntry {
            val urlHash = buffer.long
            val md5Hi = buffer.long
            val md5Lo = buffer.long
            return DistributedHashEntry(urlHash, md5Hi, md5Lo)
        }

    }


}
