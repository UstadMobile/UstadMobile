package com.ustadmobile.core.io

import com.ustadmobile.core.io.ext.toConcatenatedEntry
import com.ustadmobile.door.ext.toHexString
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Integer.min
import java.security.MessageDigest
import com.ustadmobile.door.util.NullOutputStream
import com.ustadmobile.core.io.ext.readFully
import io.github.aakira.napier.Napier


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

    //MD5Sum of data for the current entry as it has been read through this stream
    private var currentEntryReadMd5: ByteArray? = null

    private fun assertDataReadMatchesMd5() {
        val entryVal = currentEntry ?: throw IOException("No current entry: cannot verify data")
        val dataMd5 = currentEntryReadMd5 ?: inflateMessageDigest.digest()

        if(!dataMd5.contentEquals(entryVal.md5))
            throw ConcatenatedDataIntegrityException("Data read was corrupted: md5 does not match! " +
                    "Expected MD5: ${entryVal.md5.toHexString()} / Actual ${dataMd5.toHexString()}")

        currentEntryReadMd5 = dataMd5
    }

    //Read the remainder of the current entry. This is required to ensure that md5sums will match
    //when verified
    private fun readCurrentEntryRemaining() {
        if(entryRemaining > 0)
            copyTo(NullOutputStream())
    }

    /**
     * This function can be used by the consumer to verify that the current entry has been fully
     * read. Sometimes the consumer might want to verify this before calling getNextEntry
     */
    fun verifyCurrentEntryCompleted() {
        if(entryRemaining != 0L)
            throw IOException("verifyCurrentEntryCompleted: entry ${currentEntry?.md5?.toHexString()} " +
                    "is not completed")

        assertDataReadMatchesMd5()
    }

    /**
     * Get the next entry
     */
    fun getNextEntry() : ConcatenatedEntry? {
        readCurrentEntryRemaining()

        // Catch premature end of stream...
        // e.g. if entryRemaining != 0
        if(entryRemaining > 0L) {
            throw IOException("Premature end of stream: ${currentEntry?.md5?.toHexString()} has " +
                    "$entryRemaining bytes unread")
        }

        if(currentEntry != null) {
            assertDataReadMatchesMd5()
        }

        currentEntryReadMd5 = null

        val entryBuf = ByteArray(ConcatenatedEntry.SIZE)

        val bytesRead = super.`in`.readFully(entryBuf, 0, entryBuf.size)
        if(bytesRead == entryBuf.size) {
            val nextEntry = entryBuf.toConcatenatedEntry()
            entryRemaining = nextEntry.compressedSize

            inflateMessageDigest.reset(inflateEnabled = nextEntry.isCompressed)

            currentEntry = nextEntry
            return nextEntry
        }else {
            //Hit end of stream
            entryRemaining = 0
            currentEntry = null
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
            if(entryRemaining == 0L)
                assertDataReadMatchesMd5()

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
        if(bytesRead != -1) {
            inflateMessageDigest.update(buf, offset, bytesRead)
            entryRemaining -= bytesRead
        }

        if(entryRemaining == 0L) {
            assertDataReadMatchesMd5()
        }

        return bytesRead
    }

    override fun close(){
        Napier.d("ConcatenatedInputStream: entry ${currentEntry?.md5?.toHexString()} : close")
        readCurrentEntryRemaining()

        if(currentEntry != null && entryRemaining == 0L) {
            Napier.d("ConcatenatedInputStream: entry ${currentEntry?.md5?.toHexString()} : close check last entry")
            assertDataReadMatchesMd5()
        }


        super.close()
    }
}
