/*
    JSPWiki - a JSP-based WikiWiki clone.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.    
 */
package com.ustadmobile.core.util


import kotlinx.io.charsets.Charset
import kotlinx.io.core.String
import kotlinx.io.core.toByteArray
import kotlinx.serialization.toUtf8Bytes


/**
 * Contains a number of static utility methods.
 * Taken from
 * http://www.java2s.com/Tutorial/Java/0320__Network/URLencoding.htm
 */
// FIXME3.0: Move to the "util" package
/**
 * Private constructor prevents instantiation.
 */
class URLTextUtil {
    companion object {
        internal val HEX_DIGITS = "0123456789ABCDEF"

        /**
         * java.net.URLEncoder.encode() method in JDK < 1.4 is buggy.  This duplicates
         * its functionality.
         * @param rs the string to encode
         * @return the URL-encoded string
         */
        protected fun urlEncode(rs: ByteArray): String {
            val result = StringBuilder(rs.size * 2)

            // Does the URLEncoding.  We could use the java.net one, but
            // it does not eat byte[]s.

            for (i in rs.indices) {
                val c = rs[i].toChar()

                when (c) {
                    '_', '.', '*', '-', '/' -> result.append(c)

                    ' ' -> result.append('+')

                    else -> if (c >= 'a' && c <= 'z' ||
                            c >= 'A' && c <= 'Z' ||
                            c >= '0' && c <= '9') {
                        result.append(c)
                    } else {
                        result.append('%')
                        result.append(HEX_DIGITS[c.toInt() and 0xF0 shr 4])
                        result.append(HEX_DIGITS[c.toInt() and 0x0F])
                    }
                }

            } // for

            return result.toString()
        }

        /**
         * URL encoder does not handle all characters correctly.
         * See <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4257115.html">
         * Bug parade, bug #4257115</A> for more information.
         * <P>
         * Thanks to CJB for this fix.
         *
         * @param bytes The byte array containing the bytes of the string
         * @param encoding The encoding in which the string should be interpreted
         * @return A decoded String
         *
         * @throws UnsupportedEncodingException If the encoding is unknown.
         * @throws IllegalArgumentException If the byte array is not a valid string.
        </P> */
        protected fun urlDecode(bytes: ByteArray?, encoding: String): String? {
            if (bytes == null) {
                return null
            }

            val decodeBytes = ByteArray(bytes.size)
            var decodedByteCount = 0

            try {
                var count = 0
                while (count < bytes.size) {
                    when (bytes[count]) {
                        '+'.toByte() -> decodeBytes[decodedByteCount++] = ' '.toByte()

                        '%'.toByte() -> decodeBytes[decodedByteCount++] = ((HEX_DIGITS.indexOf(HEX_DIGITS, bytes[++count].toInt()) shl 4) + HEX_DIGITS.indexOf(HEX_DIGITS, bytes[++count].toInt())).toByte()

                        else -> decodeBytes[decodedByteCount++] = bytes[count]
                    }
                    count++
                }

            } catch (ae: IndexOutOfBoundsException) {
                throw IllegalArgumentException("Malformed UTF-8 string?")
            }

            var processedPageName: String?

            processedPageName = String(decodeBytes, 0, decodedByteCount, Charset.forName(encoding))

            return processedPageName
        }

        /**
         * As java.net.URLEncoder class, but this does it in UTF8 character set.
         *
         * @param text The text to decode
         * @return An URLEncoded string.
         */
        fun urlEncodeUTF8(text: String?): String {
            // If text is null, just return an empty string
            if (text == null) {
                return ""
            }

            val rs: ByteArray = text.toUtf8Bytes()

            return urlEncode(rs)
        }

        /**
         * As java.net.URLDecoder class, but for UTF-8 strings.  null is a safe
         * value and returns null.
         *
         * @param utf8 The UTF-8 encoded string
         * @return A plain, normal string.
         */
        fun urlDecodeUTF8(utf8: String?): String? {
            val rs: String? = urlDecode(utf8?.toByteArray(Charset.forName("ISO-8859-1")), "UTF-8")

            if (utf8 == null) return null

            return rs
        }

        /**
         * Provides encoded version of string depending on encoding.
         * Encoding may be UTF-8 or ISO-8859-1 (default).
         *
         *
         * This implementation is the same as in
         * FileSystemProvider.mangleName().
         *
         * @param data A string to encode
         * @param encoding The encoding in which to encode
         * @return An URL encoded string.
         */
        fun urlEncode(data: String, encoding: String): String {
            // Presumably, the same caveats apply as in FileSystemProvider.
            // Don't see why it would be horribly kludgy, though.
            if ("UTF-8" == encoding) {
                return urlEncodeUTF8(data)
            }
            return urlEncode(data.toByteArray(Charset.forName(encoding)))

        }

        /**
         * Provides decoded version of string depending on encoding.
         * Encoding may be UTF-8 or ISO-8859-1 (default).
         *
         *
         * This implementation is the same as in
         * FileSystemProvider.unmangleName().
         *
         * @param data The URL-encoded string to decode
         * @param encoding The encoding to use
         * @return A decoded string.
         * @throws UnsupportedEncodingException If the encoding is unknown
         * @throws IllegalArgumentException If the data cannot be decoded.
         */
        fun urlDecode(data: String, encoding: String): String? {
            // Presumably, the same caveats apply as in FileSystemProvider.
            // Don't see why it would be horribly kludgy, though.
            if ("UTF-8" == encoding) {
                return urlDecodeUTF8(data)
            }
            return urlDecode(data.toByteArray(Charset.forName(encoding)), encoding)

        }
    }

}