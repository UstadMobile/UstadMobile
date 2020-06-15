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

package com.ustadmobile.core.util

import com.ustadmobile.core.impl.UMLog
import kotlinx.io.ByteArrayOutputStream
import kotlinx.io.InputStream
import kotlinx.io.OutputStream
import kotlinx.serialization.stringFromUtf8Bytes
import org.kmp.io.KMPPullParserException
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.math.max
import kotlin.reflect.KClass

/**
 *
 * @author mike
 */
object UMIOUtils {

    val HTTP_SIZE_NOT_GIVEN = -1

    val HTTP_SIZE_IO_EXCEPTION = -2

    val DEFAULT_BUFFER_SIZE = 8 * 1024

    /**
     * Close the given input stream if not null
     *
     * @param in An input stream to close
     */
    @JvmStatic
    fun closeInputStream(`in`: InputStream?) {
        try {
            `in`?.close()
        } catch (e: Exception) {
        }

    }

    /**
     * Close the given output stream if not null
     *
     * @param out An input stream to close
     * @param flush if true - flush the stream before closing
     */
    @JvmOverloads
    @JvmStatic
    fun closeOutputStream(out: OutputStream?, flush: Boolean = false) {
        try {
            if (out != null) {
                if (flush)
                    out.flush()
                out.close()
            }
        } catch (e: Exception) {

        }

    }


    /**
     * Read from the given input stream and write to the given output stream.
     * This will not close the streams themselves
     */
    @JvmOverloads
    @JvmStatic
    fun readFully(`in`: InputStream, out: OutputStream, bufsize: Int = DEFAULT_BUFFER_SIZE) {
        val buf = ByteArray(bufsize)
        var bytesRead: Int = -1

        while ({bytesRead = `in`.read(buf); bytesRead}() != -1) {
            out.write(buf, 0, bytesRead)
        }
        out.flush()
    }

    @ExperimentalStdlibApi
    @JvmOverloads
    @JvmStatic
    fun readStreamToString(`in`: InputStream, bufsize: Int = DEFAULT_BUFFER_SIZE): String {
        val bout = ByteArrayOutputStream()
        readFully(`in`, bout, bufsize)
        `in`.close()

        return  bout.toByteArray().decodeToString() //stringFromUtf8Bytes(bout.toByteArray())
    }

    @JvmOverloads
    @JvmStatic
    fun readStreamToByteArray(`in`: InputStream, bufsize: Int = DEFAULT_BUFFER_SIZE): ByteArray {
        val bout = ByteArrayOutputStream()
        readFully(`in`, bout, bufsize)
        return bout.toByteArray()
    }





    fun throwIfNotNullIO(e: Exception?) {
        if (e != null) {
            throw e
        }
    }

    /**
     * Throw the given exception if it's not null
     *
     * @param throwable Throwable exception
     * @param clazz Exception class
     * @param <T> Exception class type
     *
     * @throws T Throwable exception
    </T> */
    fun <T : Throwable> throwIfNotNull(throwable: T?, clazz: KClass<T>) {
        if (throwable != null)
            throw throwable
    }


    fun throwIfNotNullXPE(xe: KMPPullParserException?) {
        if (xe != null) {
            throw xe
        }
    }



    fun sanitizeIDForFilename(id: String): String {
        var c: Char
        val len = id.length
        val retVal = StringBuilder()
        for (i in 0 until len) {
            c = id[i]
            if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c == '.' || c == '-' || c == '*' || c == '_') {
                retVal.append(c)
            } else if (c == ' ' || c == '\t' || c == '\n') {
                retVal.append('_')
            } else {
                retVal.append("_").append(convertToHexString(c.toInt()))
            }
        }
        return retVal.toString()
    }

    fun convertToHexString(`val`: Int, shift: Int = 4): String {
        // assert shift > 0 && shift <=5 : "Illegal shift value";
        val mag = 32 - numberOfLeadingZeros(`val`)
        val chars = max((mag + (shift - 1)) / shift, 1)
        val buf = CharArray(chars)

        formatUnsignedInt(`val`, shift, buf, 0, chars)

        return String(buf)
    }

    private fun numberOfLeadingZeros(i: Int): Int {
        var i = i
        // HD, Figure 5-6
        if (i == 0)
            return 32
        var n = 1
        if (i.ushr(16) == 0) {
            n += 16
            i = i shl 16
        }
        if (i.ushr(24) == 0) {
            n += 8
            i = i shl 8
        }
        if (i.ushr(28) == 0) {
            n += 4
            i = i shl 4
        }
        if (i.ushr(30) == 0) {
            n += 2
            i = i shl 2
        }
        n -= i.ushr(31)
        return n
    }

    private val digits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')

    /**
     * Format a long (treated as unsigned) into a character buffer.
     * @param val the unsigned int to format
     * @param shift the log2 of the base to format in (4 for hex, 3 for octal, 1 for binary)
     * @param buf the character buffer to write to
     * @param offset the offset in the destination buffer to start at
     * @param len the number of characters to write
     * @return the lowest character  location used
     */
    private fun formatUnsignedInt(`val`: Int, shift: Int, buf: CharArray, offset: Int, len: Int): Int {
        var `val` = `val`
        var charPos = len
        val radix = 1 shl shift
        val mask = radix - 1
        do {
            buf[offset + --charPos] = digits[`val` and mask]
            `val` = `val` ushr shift
        } while (`val` != 0 && charPos > 0)

        return charPos
    }


}