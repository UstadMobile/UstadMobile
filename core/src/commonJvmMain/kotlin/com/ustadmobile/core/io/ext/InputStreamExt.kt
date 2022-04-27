package com.ustadmobile.core.io.ext

import com.ustadmobile.core.io.ChecksumResults
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.zip.GZIPOutputStream
import com.ustadmobile.retriever.io.MultiDigestOutputStream
import java.security.MessageDigest
import java.util.zip.CRC32
import com.ustadmobile.retriever.ext.copyToAsync

/**
 * As per the InputStream spec it is possible that an InputStream might only read up to and
 * including the number of bytes specified based on availability. readFully will continue attempting
 * to read bytes until all bytes requested have been read, or until the end of the stream has been
 * reached.
 */
fun InputStream.readFully(buf: ByteArray, offset: Int = 0, len: Int = buf.size): Int {
    var totalBytesRead = 0
    while(totalBytesRead < len) {
        val bytesRead = read(buf, offset + totalBytesRead, len - totalBytesRead)
        if(bytesRead == -1) {
            /* If no bytes have been read, and we already have -1 returned from attempting to read
             * the stream itself, then there is nothing left. We need to return -1 now. Otherwise
             * we can return the number of bytes actually read.
             */
            if(totalBytesRead == 0)
                return -1
            else
                return totalBytesRead
        }

        totalBytesRead += bytesRead
    }

    return totalBytesRead
}

suspend fun InputStream.writeToFileAsync(destination: File){
    withContext(Dispatchers.IO){
        use { inStream ->
            FileOutputStream(destination).use { outStream ->
                var bytesCopied: Long = 0
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytes = inStream.read(buffer)
                while (bytes >= 0 && isActive) {
                    outStream.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    bytes = inStream.read(buffer)
                }
                outStream.flush()
            }
        }
    }
}

/**
 * Copy the given InputStream to the destination output stream and collect SHA256, MD5, and CRC as
 * the data is being copied.
 *
 * @param destination destination output stream. Note: This output WILL be closed by this function.
 *        this must be done to ensure that gzip data is written correctly.
 * @param gzip if true, then the output will be gzipped
 * @param md5MessageDigest MessageDigest instance that will be used to get the MD5 sum. This will
 *        apply to the original input data (e.g. if gzip is enabled, the md5 is the md5 of the
 *        uncompressed data)
 * @param sha256MessageDigest MessageDigest instance that will be used to get the SHA-256 sum. If
 *        gzip is enabled, this is for the sha-256 of the gzipped data (not the original).
 * @param crc32 crc32 If gzip is enabled, this is for crc32 of the gzipped data (not the original).
 */
suspend fun InputStream.copyToAndGetDigests(
    destination: OutputStream,
    gzip: Boolean,
    md5MessageDigest: MessageDigest,
    sha256MessageDigest: MessageDigest,
    crc32: CRC32,
) : ChecksumResults {
    return withContext(Dispatchers.IO) {
        val outStream = if(gzip) {
            //sha256 and crc32 values apply to the compressed data
            val gzipOut = GZIPOutputStream(MultiDigestOutputStream(destination,
                arrayOf(sha256MessageDigest), crc32))
            //md5 applies to the original data
            MultiDigestOutputStream(gzipOut, arrayOf(md5MessageDigest))
        }else {
            MultiDigestOutputStream(destination, arrayOf(md5MessageDigest, sha256MessageDigest), crc32)
        }

        outStream.use { output ->
            copyToAsync(output)
            outStream.flush()
        }

        ChecksumResults(sha256MessageDigest.digest(), md5MessageDigest.digest(), crc32.value)
    }
}

/**
 * Copy the given InputStream to the destination file and collect SHA256, MD5, and CRC as
 * the data is being copied.
 *
 * @param destination destination file
 * @param gzip if true, then the output will be gzipped
 * @param md5MessageDigest MessageDigest instance that will be used to get the MD5 sum. This will
 *        apply to the original input data (e.g. if gzip is enabled, the md5 is the md5 of the
 *        uncompressed data)
 * @param sha256MessageDigest MessageDigest instance that will be used to get the SHA-256 sum. If
 *        gzip is enabled, this is for the sha-256 of the gzipped data (not the original).
 * @param crc32 crc32 If gzip is enabled, this is for crc32 of the gzipped data (not the original).
 */
@Suppress("BlockingMethodInNonBlockingContext")
suspend fun InputStream.copyToAndGetDigests(
    destination: File,
    gzip: Boolean,
    md5MessageDigest: MessageDigest,
    sha256MessageDigest: MessageDigest,
    crc32: CRC32,
) : ChecksumResults {
    return FileOutputStream(destination).use { fileOut ->
        copyToAndGetDigests(fileOut, gzip, md5MessageDigest, sha256MessageDigest, crc32)
    }
}


fun InputStream.readString(): String{
    return this.bufferedReader().use { it.readText() }
}