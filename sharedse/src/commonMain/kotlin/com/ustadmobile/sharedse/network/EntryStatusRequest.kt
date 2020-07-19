package com.ustadmobile.sharedse.network

import com.ustadmobile.sharedse.io.ByteBufferSe
import io.ktor.utils.io.core.toByteArray

/**
 * Represents a request from one device to another to check if it has a given set of entries
 *
 * This has a toBytes and fromBytes function to serialize directly to bytes (to minimize size when
 * transferring a list of Longs that would be much larger if sent using Json). These are typically
 * sent over bandwidth limited BLE connections.
 *
 * It is serialized as follows:
 * Bytes 0-3 : Length of endpointUrl bytes
 * 4-(4 + endpointUrlBytes): Endpoint url string in bytes
 * 4+ endpointUrlBytes - end: Raw Longs that represent the contentEntryUids that the device wants a
 * status for
 */
data class EntryStatusRequest(val endpointUrl: String, val entryList: LongArray) {

    fun toBytes(): ByteArray {
        val endpointUrlBytes = endpointUrl.toByteArray()

        //4 byte integer representing length of endpoint url string, then the string itself, then the entryList
        val buffer = ByteBufferSe.allocate(4 + endpointUrlBytes.size + (entryList.size * 8))
        buffer.putInt(endpointUrlBytes.size)
        buffer.put(endpointUrlBytes)
        entryList.forEach {
            buffer.putLong(it)
        }

        return buffer.array()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EntryStatusRequest

        if (endpointUrl != other.endpointUrl) return false
        if (!entryList.contentEquals(other.entryList)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = endpointUrl.hashCode()
        result = 31 * result + entryList.contentHashCode()
        return result
    }

    companion object {
        fun fromBytes(byteArray: ByteArray): EntryStatusRequest {
            val buffer = ByteBufferSe.wrap(byteArray)
            val endpointLen = buffer.getInt()
            val endpointBytes = ByteArray(endpointLen)
            buffer.get(endpointBytes, 0, endpointLen)
            val entryList = LongArray(buffer.remaining() / 8)
            for(index in 0 until entryList.size) {
                entryList[index] = buffer.getLong()
            }


            return EntryStatusRequest(endpointBytes.decodeToString(), entryList)
        }
    }

}