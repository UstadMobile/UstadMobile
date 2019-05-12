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

import com.ustadmobile.core.impl.UMLogger
import com.ustadmobile.core.impl.UstadMobileSystemImpl

import org.xmlpull.v1.XmlPullParserException

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

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
        } catch (e: IOException) {
        }

    }

    fun closeQuietly(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: IOException) {

            }

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
        } catch (e: IOException) {

        }

    }


    /**
     * Read from the given input stream and write to the given output stream.
     * This will not close the streams themselves
     */
    @Throws(IOException::class)
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

    @Throws(IOException::class)
    @JvmOverloads
    @JvmStatic
    fun readStreamToString(`in`: InputStream, bufsize: Int = DEFAULT_BUFFER_SIZE): String {
        val bout = ByteArrayOutputStream()
        readFully(`in`, bout, bufsize)
        `in`.close()

        return String(bout.toByteArray(), Charset.forName("UTF-8"))
    }

    @Throws(IOException::class)
    @JvmOverloads
    @JvmStatic
    fun readStreamToByteArray(`in`: InputStream, bufsize: Int = DEFAULT_BUFFER_SIZE): ByteArray {
        val bout = ByteArrayOutputStream()
        readFully(`in`, bout, bufsize)
        return bout.toByteArray()
    }


    /**
     * Read from the given input stream and return a string
     *
     * @param in Input Stream to read from
     * @param encoding Encoding to use
     * @return String from the given input stream in the given encoding
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readToString(`in`: InputStream, encoding: String): String {
        val bout = ByteArrayOutputStream()
        readFully(`in`, bout, 1024)
        `in`.close()
        return String(bout.toByteArray(), Charset.forName(encoding))
    }


    @Throws(IOException::class)
    fun throwIfNotNullIO(e: IOException?) {
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
    @Throws(IOException::class)
    fun <T : Throwable> throwIfNotNull(throwable: T?, clazz: Class<T>) {
        if (throwable != null)
            throw throwable
    }


    @Throws(XmlPullParserException::class)
    fun throwIfNotNullXPE(xe: XmlPullParserException?) {
        if (xe != null) {
            throw xe
        }
    }


    /**
     * Logs and throws the given exception if it is not null
     *
     * @param e Exception (Null if no exception happened)
     * @param level Exception level
     * @param code Exception code
     * @param message Message if any
     *
     * @see UMLogger
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun logAndThrowIfNotNullIO(e: IOException?, level: Int, code: Int, message: String) {
        if (e != null) {
            UstadMobileSystemImpl.l(level, code, message, e)
            throw e
        }
    }

    @Throws(Exception::class)
    fun throwIfNotNull(e: Exception?) {
        if (e != null) {
            throw e
        }
    }

    fun sanitizeIDForFilename(id: String): String {
        var c: Char
        val len = id.length
        val retVal = StringBuffer()
        for (i in 0 until len) {
            c = id[i]
            if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c == '.' || c == '-' || c == '*' || c == '_') {
                retVal.append(c)
            } else if (c == ' ' || c == '\t' || c == '\n') {
                retVal.append('_')
            } else {
                retVal.append("_").append(Integer.toHexString(c.toInt()))
            }
        }
        return retVal.toString()
    }


}