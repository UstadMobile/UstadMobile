/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */

package com.ustadmobile.port.sharedse.impl.http

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Simple InputStream filter to support range requests.  It bounds the source input stream to serve
 * from a given start byte up until a given end byte.
 *
 * Created by mike on 12/18/15.
 */
class RangeInputStream
/**
 * Create a RangeInputStream that bounds the given source input streamL this is useful for
 * fulfilling range http requests from any abstract InputStream.
 *
 * @param in Source InputStream
 * @param start The first byte to serve: The stream will skip this many bytes from the in stream
 * @param end The end of the range to serve up to.  After this even if the source stream has more
 * bytes this stream will return -1 to signal the end of the stream
 * @throws IOException
 */
@Throws(IOException::class)
constructor(`in`: InputStream, start: Long, private val end: Long) : FilterInputStream(`in`) {

    private var pos: Long = 0

    private val markSupported: Boolean

    private var resetPos: Long = 0

    private var resetInvalidate: Long = 0


    init {
        markSupported = `in`.markSupported()
        resetPos = -1
        resetInvalidate = -1
        pos = 0

        //skip can skip up to the requested number of bytes to skip
        var startBytesSkipped: Long = 0
        while (startBytesSkipped < start) {
            startBytesSkipped += skip(start - startBytesSkipped)
        }
    }

    @Throws(IOException::class)
    override fun read(): Int {
        if (pos <= end) {
            pos++
            return `in`.read()
        } else {
            return -1
        }
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray): Int {
        return read(buffer, 0, buffer.size)
    }


    @Throws(IOException::class)
    override fun read(buffer: ByteArray, byteOffset: Int, byteCount: Int): Int {
        var byteCount = byteCount
        byteCount = Math.min(end + 1 - pos, byteCount.toLong()).toInt()
        if (byteCount > 0) {
            val bytesRead = `in`.read(buffer, byteOffset, byteCount)
            pos += bytesRead.toLong()
            return bytesRead
        } else {
            return -1
        }
    }

    @Throws(IOException::class)
    override
            /**
             * ===WORKAROUND===
             *
             * NanoHTTPD is using InputStream.available incorrectly: as if it provides the
             * pending number of bytes in the stream.  As per the Java documentation it in
             * fact is only supposed to reply with how many bytes it can deliver before
             * blocking
             */
    fun available(): Int {
        //        return (int)((end +1)- pos);
        return super.available()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun reset() {
        if (resetPos != -1L && pos < resetInvalidate) {
            `in`.reset()
            pos = resetPos
        }
    }

    @Throws(IOException::class)
    override fun skip(byteCount: Long): Long {
        val skipped = `in`.skip(byteCount).toInt()
        pos += skipped.toLong()
        return skipped.toLong()
    }

    @Synchronized
    override fun mark(readlimit: Int) {
        if (markSupported) {
            resetPos = pos
            resetInvalidate = pos + readlimit
        }

        super.mark(readlimit)
    }
}
