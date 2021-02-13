package com.ustadmobile.core.io.ext

import java.io.InputStream

/**
 * As per the InputStream spec it is possible that an InputStream might only read up to and
 * including the number of bytes specified based on availability. readFully will continue attempting
 * to read bytes until all bytes requested have been read, or until the end of the stream has been
 * reached.
 */
fun InputStream.readFully(buf: ByteArray, offset: Int, len: Int): Int {
    var totalBytesRead = 0
    while(totalBytesRead < len) {
        val bytesRead = read(buf, offset + totalBytesRead, len - totalBytesRead)
        if(bytesRead == -1)
            return totalBytesRead

        totalBytesRead += bytesRead
    }

    return totalBytesRead
}