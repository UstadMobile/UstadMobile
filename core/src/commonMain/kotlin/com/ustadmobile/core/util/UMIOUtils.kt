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

import kotlinx.io.ByteArrayOutputStream
import kotlinx.io.InputStream
import kotlinx.io.OutputStream
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

    const val HTTP_SIZE_NOT_GIVEN = -1

    const val HTTP_SIZE_IO_EXCEPTION = -2

    const val DEFAULT_BUFFER_SIZE = 8 * 1024

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


}