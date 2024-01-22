package com.ustadmobile.core.io

import com.ustadmobile.core.io.ext.toBytes
import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream
import java.security.MessageDigest

/**
 * Creates a ConcatenatedOutputStream. A ConcatenatedOutputStream is a simple stream containing
 * multiple fixed-size entries. Each entry has an md5sum, a length, and may be gzipped.
 *
 *  ConcatenatedEntry header - 25 bytes| entry data - num of bytes as specified as header .
 *
 * The md5sum of the actual data written will be checked against the md5sum provided for the entry
 * each time a new entry is added or when the stream is closed. An exception will be thrown if
 * the md5sum of the data actually written does not match the provided md5.
 *
 * This class works similarly to ZipOutputStream. To use call putNextEntry to start an entry,
 * then write the data of the entry itself. This can be repeated as many times as desired. Then
 * call close.
 *
 */
@OptIn(ExperimentalStdlibApi::class)
class ConcatenatedOutputStream2(outputStream: OutputStream,
                                messageDigest: MessageDigest = MessageDigest.getInstance("MD5"),
                                internal val verifyMd5: Boolean = true) : FilterOutputStream(outputStream) {

    private var currentEntry: ConcatenatedEntry? = null

    private val inflateMessageDigest = GzipMessageDigest(messageDigest)

    private val oneByteBuffer = ByteArray(1)

    private fun assertHasCurrentEntry(){
        if(currentEntry == null)
            throw IOException("ConcatenatedOutputStream2: No current entry: you must call putNextEntry before writing")
    }

    private fun assertMd5MatchesCurrentEntry() {
        val entryVal = currentEntry ?: throw IOException("No current entry to verify against.")
        val currentMd5 = inflateMessageDigest.digest()
        if(!verifyMd5)
            return

        if(!currentMd5.contentEquals(entryVal.md5))
            throw ConcatenatedDataIntegrityException("MD5 provided for entry " +
                    "${entryVal.md5.toHexString()} does not match the MD5 of the data written " +
                    "${currentMd5.toHexString()}!")

    }

    override fun write(p0: Int) {
        assertHasCurrentEntry()
        oneByteBuffer[0]  = p0.toByte()
        inflateMessageDigest.update(oneByteBuffer)

        out.write(p0)
    }

    override fun write(buf: ByteArray) = write(buf, 0, buf.size)

    override fun write(buf: ByteArray, offset: Int, len: Int) {
        assertHasCurrentEntry()
        inflateMessageDigest.update(buf, offset, len)
        out.write(buf, offset, len)
    }

    /**
     * Puts the next entry into the stream. This will verify the last entry written (if any)
     * and write the header for the given next entry. If the last entry failed to verify, then an
     * IOException will be thrown.
     *
     * @param entry Next concatenated entry to write
     */
    fun putNextEntry(entry: ConcatenatedEntry) {
        //checksum the last entry
        if(currentEntry != null){
            assertMd5MatchesCurrentEntry()
        }

        //now write the header
        out.write(entry.toBytes())
        currentEntry = entry
        inflateMessageDigest.reset(inflateEnabled = entry.isCompressed)
    }

    override fun close(){
        assertMd5MatchesCurrentEntry()
        super.close()
    }

}
