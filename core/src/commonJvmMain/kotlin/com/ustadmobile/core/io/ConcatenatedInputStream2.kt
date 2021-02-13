package com.ustadmobile.core.io

import com.ustadmobile.core.io.ext.toConcatenatedEntry
import com.ustadmobile.door.ext.toHexString
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Integer.min
import java.security.MessageDigest
import com.ustadmobile.core.io.ext.readFully
import com.ustadmobile.door.util.NullOutputStream

/**
 * Reads concatenated data that was written using ConcatenatedOutputStream2. It is used similarly
 * to ZipInputStream . First getNextEntry should be called. After calling getNextEntry, it is
 * possible to read the data of that entry. The consumer should continue calling getNextEntry until
 * it returns null
 */
class ConcatenatedInputStream2(inputStream: InputStream, messageDigest: MessageDigest = MessageDigest.getInstance("MD5")) : FilterInputStream(inputStream) {

    private var currentEntry: ConcatenatedEntry? = null

    private var entryRemaining: Long = -1

    private val inflateMessageDigest = GzipMessageDigest(messageDigest)

    private val oneByteBuffer = ByteArray(1)

    private fun assertDataReadMatchesMd5() {
        val entryVal = currentEntry ?: throw IOException("No current entry: cannot verify data")
        val dataMd5 = inflateMessageDigest.digest()
        if(!dataMd5.contentEquals(entryVal.md5))
            throw IOException("Data read was corrupted: md5 does not match! " +
                    "Expected MD5: ${entryVal.md5.toHexString()} / Actual ${dataMd5.toHexString()}")
    }

    //Read the remainder of the current entry. This is required to ensure that md5sums will match
    //when verified
    private fun readCurrentEntryRemaining() {
        if(entryRemaining > 0)
            copyTo(NullOutputStream())
    }

    /**
     * Get the next entry
     */
    fun getNextEntry() : ConcatenatedEntry? {
        readCurrentEntryRemaining()

        if(currentEntry != null)
            assertDataReadMatchesMd5()

        val entryBuf = ByteArray(ConcatenatedEntry.SIZE)

        val bytesRead = super.`in`.readFully(entryBuf, 0, entryBuf.size)
        if(bytesRead == entryBuf.size) {
            val nextEntry = entryBuf.toConcatenatedEntry()
            entryRemaining = nextEntry.compressedSize

            inflateMessageDigest.reset(inflateEnabled = nextEntry.isCompressed)

            currentEntry = nextEntry
            return nextEntry
        }else {
            return null
        }
    }

    override fun read(buf: ByteArray) = read(buf, 0, buf.size)

    override fun read(): Int {
        if(entryRemaining > 0) {
            val byteRead = super.read()
            oneByteBuffer[0] = byteRead.toByte()
            inflateMessageDigest.update(oneByteBuffer)
            entryRemaining--
            return byteRead
        }else {
            return -1
        }
    }

    override fun read(buf: ByteArray, offset: Int, len: Int): Int {
        val lenToRead = min(len, entryRemaining.toInt())
        if(lenToRead == 0)
            return -1

        val bytesRead = super.read(buf, offset, lenToRead)
        inflateMessageDigest.update(buf, offset, bytesRead)
        entryRemaining -= bytesRead
        return bytesRead
    }

    override fun close(){
        readCurrentEntryRemaining()
        assertDataReadMatchesMd5()
        super.close()
    }
}
