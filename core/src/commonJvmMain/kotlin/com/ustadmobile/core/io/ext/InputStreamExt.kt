package com.ustadmobile.core.io.ext

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

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


fun InputStream.readString(): String{
    return this.bufferedReader().use { it.readText() }
}