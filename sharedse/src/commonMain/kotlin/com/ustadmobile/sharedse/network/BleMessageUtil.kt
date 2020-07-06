package com.ustadmobile.sharedse.network

import com.ustadmobile.sharedse.io.ByteBufferSe

/**
 * Util class for {[BleMessage]}, it converts the entry UUID from Long to Bytes and vice versa.
 */
object BleMessageUtil {

    /**
     * Convert a list of entries in {[Long]}  to {[Byte]} arrays
     * @param entryList List of entry UUID in {[Long]}
     * @return Converted bytes
     */
    fun bleMessageLongToBytes(entryList: List<Long>): ByteArray {
        val buffer = ByteBufferSe.allocate(entryList.size * 8)
        for (entry in entryList) {
            buffer.putLong(entry)
        }
        return buffer.array()
    }

    /**
     * Convert entries from {[Byte]} array back to List of {[Long]}
     * @param entryInBytes Entry Id' in byte arrays.
     * @return Constructed list of {[Long]}
     */
    fun bleMessageBytesToLong(entryInBytes: ByteArray): List<Long> {
        val entries = ArrayList<Long>()
        val BUFFER_SIZE = 8
        var start = 0
        for (position in 0 until entryInBytes.size / BUFFER_SIZE) {
            val end = start + BUFFER_SIZE
            //  long entry = ByteBuffer.wrap(Arrays.copyOfRange(entryInBytes, start, end)).getLong();
            val entry = ByteBufferSe.wrap(entryInBytes.copyOfRange(start, end)).getLong()
            entries.add(entry)
            start += BUFFER_SIZE
        }
        return entries

    }

}
