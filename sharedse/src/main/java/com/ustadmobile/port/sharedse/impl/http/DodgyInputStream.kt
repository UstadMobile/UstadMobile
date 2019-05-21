/*
    This file is part of Dodgy HTTP Server.

    Dodgy HTTP Server Copyright (C) 2011-2014 UstadMobile Inc.

    Dodgy HTTP Server is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Dodgy HTTP Server is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.port.sharedse.impl.http

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

/**
 * This class is designed to help simulate a lousy connection.  It can slow down
 * bytes (using Thread.sleep in accordance with a speed limit) and it can for no
 * good reason other than you telling it to force an exception to be thrown.
 *
 * @author mike
 */
class DodgyInputStream
/**
 *
 * @param src Original source input stream
 * @param speedLimit in bytes/second - the maximum speed at which bytes can be read.  0 for no speed limit
 * @param cutoffAfter the number of bytes after which an exception will be thrown to simulate interruption. 0 for no cut off.
 */
(src: InputStream,
 /**
  * Speed limit in bytes per second
  */
 private val speedLimit: Int,
 /**
  * Number of bytes after which we will throw an error to cut the stream
  */
 private val cutoffAfter: Int) : FilterInputStream(src) {

    /**
     * Counting the number of bytes read total used to trigger determine when we cutoff
     */
    private var bytesRead: Int = 0

    init {
        bytesRead = 0
    }

    @Throws(IOException::class)
    override fun read(): Int {
        if (speedLimit != 0) {
            try {
                Thread.sleep((1000 / speedLimit).toLong())
            } catch (e: InterruptedException) {
            }

        }
        bytesRead++

        if (cutoffAfter > 0 && bytesRead > cutoffAfter) {
            throw IOException("Forced error after $cutoffAfter bytes")
        }

        return super.read()
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (speedLimit > 0) {
            try {
                Thread.sleep((1000 * len / speedLimit).toLong())
            } catch (e: InterruptedException) {
            }

        }

        val streamReadCount = super.read(b, off, len)
        bytesRead += streamReadCount

        if (cutoffAfter > 0 && bytesRead > cutoffAfter) {
            throw IOException("Forced error after $cutoffAfter bytes")
        }

        return streamReadCount
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

}
