package com.ustadmobile.core.io.ext

import java.io.InputStream

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