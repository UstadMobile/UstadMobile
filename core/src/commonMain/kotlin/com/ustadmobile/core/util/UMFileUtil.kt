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

import com.ustadmobile.core.util.ext.roundTo
import io.ktor.http.ContentType
import kotlin.js.JsName
import kotlin.jvm.JvmStatic
import kotlin.math.round


/**
 * Assorted cross platform file utility methods
 *
 * @author mike
 */
object UMFileUtil {

    const val FILE_SEP = '/'

    /**
     * Constant string - the file:/// protocol
     */
    const val PROTOCOL_FILE = "file:///"


    private const val UNIT_GB = 1024L * 1024L * 1024L

    private const val UNIT_MB = 1024L * 1024L

    private const val UNIT_KB: Long = 1024

    /**
     * Join multiple paths - make sure there is just one FILE_SEP character
     * between them.  Only handles situations where there could be a single extra
     * slash - e.g. "path1/" + "/somefile.txt" - does not look inside the
     * path components and does not deal with double // inside a single component
     *
     * @param paths Array of paths to join
     * @return path components joined with a single FILE_SEP character between
     */
    @JvmStatic
    fun joinPaths(vararg paths: String): String {
        return buildString {
            paths.forEachIndexed { index, component ->
                append(
                    if (index > 0) {
                        component.removePrefix("/")
                    } else {
                        component
                    }
                )

                //If not the final component - make sure it ends with a slash
                if (index < paths.size - 1 && !component.endsWith("/")) {
                    append(FILE_SEP)
                }
            }
        }
    }

    /**
     * Resolve a link relative to an absolute base.  The path to resolve could
     * itself be absolute or relative.
     *
     *
     * e.g.
     * resolvePath("http://www.server.com/some/dir", "../img.jpg");
     * returns http://www.server.com/some/img.jpg
     *
     * @param baseLink The absolute base path
     * @param link The link given relative to the base
     * @return
     */
    fun resolveLink(baseLink: String, link: String): String {
        var base = baseLink
        val linkLower = link.lowercase()
        var charFoundIndex: Int

        charFoundIndex = linkLower.indexOf("://")
        if (charFoundIndex != -1) {
            var isAllChars = true
            var cc: Char
            for (i in 0 until charFoundIndex) {
                cc = linkLower[i]
                isAllChars = isAllChars and (cc > 'a' && cc < 'z' || cc > '0' && cc < '9' || cc == '+' || cc == '.' || cc == '-')
            }

            //we found :// and all valid scheme name characters before; path itself is absolute
            if (isAllChars) {
                return link
            }
        }

        //Check if this is actually a data: link which should not be resolved
        if (link.startsWith("data:")) {
            return link
        }

        if (link.length > 2 && link[0] == '/' && link[1] == '/') {
            //we want the protocol only from the base
            return base.substring(0, base.indexOf(':') + 1) + link
        }

        if (link.length > 1 && link[0] == '/') {
            //we should start from the end of the server
            val serverStartPos = base.indexOf("://") + 3
            val serverFinishPos = base.indexOf('/', serverStartPos + 1)
            return base.substring(0, serverFinishPos) + link
        }

        //get rid of query if it's present in the base path
        charFoundIndex = base.indexOf('?')
        if (charFoundIndex != -1) {
            base = base.substring(0, charFoundIndex)
        }

        //remove the filename component if present in base path
        //if the base path ends with a /, remove that, because it will be joined to the path using a /
        charFoundIndex = base.lastIndexOf(FILE_SEP)

        //Check if this is not a relative link but has no actual folder structure in the base. E.g.
        // base = somefile.txt href=path/to/somewhere.text . As there is no folder structure there is
        // nothing to resolve against
        if (charFoundIndex == -1) {
            return link
        }

        base = base.substring(0, charFoundIndex)


        val baseParts = splitString(base, FILE_SEP)
        val linkParts = splitString(link, FILE_SEP)

        val resultVector = mutableListOf<String>()
        for (i in baseParts.indices) {
            resultVector.add(baseParts[i]!!)
        }

        for (i in linkParts.indices) {
            if (linkParts[i] == ".") {
                continue
            }

            if (linkParts[i] == "..") {
                resultVector.removeAt(resultVector.size - 1)
            } else {
                resultVector.add(linkParts[i]!!)
            }
        }

        val resultSB = StringBuilder()
        val numElements = resultVector.size
        for (i in 0 until numElements) {
            resultSB.append(resultVector.elementAt(i))
            if (i < numElements - 1) {
                resultSB.append(FILE_SEP)
            }
        }

        return resultSB.toString()
    }

    private fun isUriAbsoluteLcase(uriLower: String): Boolean {
        val charFoundIndex = uriLower.indexOf("://")
        if (charFoundIndex != -1) {
            var isAllChars = true
            var cc: Char
            for (i in 0 until charFoundIndex) {
                cc = uriLower[i]
                isAllChars = isAllChars and (cc in 'b'..'y' || cc in '1'..'8' || cc == '+' || cc == '.' || cc == '-')
            }

            //we found :// and all valid scheme name characters before; path itself is absolute
            if (isAllChars) {
                return true
            }
        }

        return false
    }

    fun isUriAbsolute(uri: String): Boolean {
        return isUriAbsoluteLcase(uri.lowercase())
    }


    /**
     * Split a string into an array of Strings at each instance of splitChar
     *
     *
     * This is roughly the same as using String.split : Unfortunately
     * String.split is not available in J2ME
     *
     * @param str       Whole string e.g. some/path/file.jpg
     * @param splitChar Character to split by - e.g. /
     * @return Array of Strings split e.g. "some", "path", "file.jpg"
     */
    private fun splitString(str: String, splitChar: Char): Array<String?> {
        val numParts = countChar(str, splitChar)
        val splitStr = arrayOfNulls<String>(numParts + 1)
        var buffer = StringBuilder()
        var partCounter = 0

        var currentChar: Char
        for (i in 0 until str.length) {
            currentChar = str[i]
            if (currentChar == splitChar) {
                splitStr[partCounter] = buffer.toString()
                partCounter++
                buffer = StringBuilder()
            } else {
                buffer.append(currentChar)
            }
        }

        //catch the last part
        splitStr[partCounter] = buffer.toString()

        return splitStr
    }

    /**
     * Join an array of Strings
     * e.g.
     * joinString(new String[]{"a", "b", "c"}, '/') returns "a/b/c"
     *
     * @param strArr   An array of Strings
     * @param joinChar the character to use to join them
     * @return A single string with each element of the array joined by joinChar
     */
    fun joinString(strArr: Array<String>, joinChar: Char): String {
        //TODO: Make this more efficient by calculating size first
        val resultSB = StringBuilder()

        val numElements = strArr.size
        for (i in 0 until numElements) {
            resultSB.append(strArr[i])
            if (i < numElements - 1) {
                resultSB.append(joinChar)
            }
        }

        return resultSB.toString()
    }


    private fun countChar(str: String, c: Char): Int {
        var count = 0
        val strLen = str.length
        for (i in 0 until strLen) {
            if (str[i] == c) {
                count++
            }
        }

        return count
    }

    /**
     * Gets the end filename (e.g. basename) from a url or path string.  Will chop off query
     * and preceeding directories: e.g
     * "/some/path/file.ext" returns "file.ext"
     * "http://server.com/path/thing.php?foo=bar" returns "thing.php"
     *
     * @param url
     * @return
     */
    fun getFilename(url: String): String {
        if (url.length == 1) {
            return if (url == "/") "" else url
        }

        var charPos = url.lastIndexOf('/', url.length - 2)

        var retVal = url
        if (charPos != -1) {
            retVal = url.substring(charPos + 1)
        }

        charPos = retVal.indexOf("?")
        if (charPos != -1) {
            retVal = url.substring(0, charPos)
        }

        return retVal
    }


    /**
     * Parse a deliminated string with keys and values like Content-Type parameters
     * and cache-control headers.  Keys can be present on their own e.g.
     * no-cache in which case the no-cache key will be in the map with a
     * blank string value.  It can also have an = sign with quoted or unquoted
     * text e.g. maxage=600 or maxage="600"
     *
     * @param str         String to parse
     * @param deliminator deliminator character
     * @return Map of parameters and values found
     */
    fun parseParams(str: String, deliminator: Char): Map<String, String> {
        var paramName: String? = null
        val params = HashMap<String, String>()
        var inQuotes = false

        val strLen = str.length
        var sb = StringBuilder()
        var c: Char

        var lastChar: Char = 0.toChar()
        for (i in 0 until strLen) {
            c = str[i]
            if (c == '"') {
                if (!inQuotes) {
                    inQuotes = true
                } else if (inQuotes && lastChar != '\\') {
                    inQuotes = false
                }

            }

            if (isWhiteSpace(c) && !inQuotes || c == '"' && i < strLen - 1) {
                //do nothing more
            } else if (c == deliminator || i == strLen - 1) {
                //check if we are here because it's the end... then we add this to bufer
                if (i == strLen - 1 && c != '"') {
                    sb.append(c)
                }

                if (paramName != null) {
                    //this is a parameter with a value
                    params[paramName] = sb.toString()
                } else {
                    //this is a parameter on its own
                    params[sb.toString()] = ""
                }

                sb = StringBuilder()
                paramName = null
            } else if (c == '=') {
                paramName = sb.toString()
                sb = StringBuilder()
            } else {
                sb.append(c)
            }

            lastChar = c
        }

        return params
    }

    /**
     * @param urlQuery
     * @return
     */
    @JsName("parseURLQueryString")
    fun parseURLQueryString(urlQuery: String): Map<String, String> {
        var retVal = urlQuery
        val queryPos = retVal.indexOf('?')
        if (queryPos != -1) {
            retVal = retVal.substring(queryPos + 1)
        }

        val parsedParams = parseParams(retVal, '&')

        return parsedParams.map { UMURLEncoder.decodeUTF8(it.key) to UMURLEncoder.decodeUTF8(it.value) }.toMap()
    }

    /**
     * Turns a map into a URL encoded query string
     *
     * @param ht map of param keys to values (keys and values must be strings)
     * @return String in the form of foo=bar&foo2=bar2 ... (URL Encoded)
     */
    @JsName("mapToQueryString")
    fun mapToQueryString(ht: Map<String, String?>): String {
        val sb = StringBuilder()

        if (ht.isEmpty()) {
            return ""
        }

        val keys = ht.keys.iterator()
        var key: String
        var firstEl = true
        while (keys.hasNext()) {
            if (!firstEl) {
                sb.append('&')
            } else {
                firstEl = false
            }

            key = keys.next()
            sb.append(UMURLEncoder.encodeUTF8(key)).append('=')
            sb.append(UMURLEncoder.encodeUTF8(ht[key] as String))
        }

        return sb.toString()
    }


    /**
     * Parse type with params header fields (Content-Disposition; Content-Type etc). E.g. given
     * application/atom+xml;type=entry;profile=opds-catalog
     *
     *
     * It will return an object with the mime type "application/atom+xml" and a map of parameters
     * with type=entry and profile=opds-catalog .
     *
     *
     * TODO: Support params with *paramname and encoding e.g. http://tools.ietf.org/html/rfc6266 section 5 example 2
     *
     * @return
     */
    fun parseTypeWithParamHeader(header: String): TypeWithParamHeader {
        val result: TypeWithParamHeader? = null

        val semiPos = header.indexOf(';')
        var typeStr: String?
        var params: Map<String, String>? = null

        typeStr = if (semiPos == -1) {
            header.trim { it <= ' ' }
        } else {
            header.substring(0, semiPos).trim { it <= ' ' }
        }

        if (semiPos != -1 && semiPos < header.length - 1) {
            params = parseParams(header.substring(semiPos), ';')
        }

        return TypeWithParamHeader(typeStr, params)
    }

    /**
     * Filter filenames for characters that could be nasty attacks (e.g. /sdcard/absolutepath etc)
     *
     * @param filename Filename from an untrusted source (e.g. http header)
     * @return Filename with sensitive characters (: / \ * > < ? ) removed
     */
    fun filterFilename(filename: String): String {
        val newStr = StringBuilder(filename.length)
        var c: Char

        for (i in 0 until filename.length) {
            c = filename[i]
            if (!(c == ':' || c == '/' || c == '\\' || c == '*' || c == '>' || c == '<' || c == '?')) {
                newStr.append(c)
            }
        }

        return newStr.toString()
    }

    /**
     * Simple wrapper class that represents a haeder field with a type
     * and parameters.
     */
    class TypeWithParamHeader(
            /**
             * The first parameter: e.g. the mime type; content disposition etc.
             */
            var typeName: String,
            /**
             * map of parameters found (case sensitive)
             */
            var params: Map<String, String>?) {

        fun getParam(paramName: String): String? {
            return if (params != null && params!!.containsKey(paramName)) {
                params!![paramName] as String
            } else {
                null
            }
        }
    }

    private fun isWhiteSpace(c: Char): Boolean {
        return c == ' ' || c == '\n' || c == '\t' || c == '\r'
    }


    /**
     * Returns the parent filename of a given string uri
     *
     * @param uri e.g. /some/file/path or http://server.com/some/file.txt
     * @return The parent e.g. /some/file or http://server.com/some/, null in case of no parent in the path
     */
    fun getParentFilename(uri: String): String? {
        if (uri.length == 1) {
            return null
        }

        val charPos = uri.lastIndexOf('/', uri.length - 2)
        return if (charPos != -1) {
            uri.substring(0, charPos + 1)
        } else {
            null
        }
    }


    /**
     * Gets the extension from a url or path string.  Will chop off the query
     * and preceeding directories, and then get the file extension.  Is returned
     * without the .
     *
     * @param uri the path or URL that we want the extension of
     * @return the extension - the last characters after the last . if there is a . in the name
     * null if no extension is found
     */
    @JvmStatic
    fun getExtension(uri: String): String? {
        val filename = getFilename(uri)
        val lastDot = filename.lastIndexOf('.')
        return if (lastDot != -1 && lastDot != filename.length - 1) {
            filename.substring(lastDot + 1)
        } else {
            null
        }
    }



    /**
     * Remove the anchor section of a link if present (e.g. for index.html#foo
     * remove #foo)
     *
     * @param uri The complete URI e.g. some/path.html#foo
     * @return the given uri without the anchor if it was found in the uri
     */
    fun stripAnchorIfPresent(uri: String): String {
        val charPos = uri.lastIndexOf('#')
        return if (charPos != -1) {
            uri.substring(0, charPos)
        } else {
            uri
        }
    }


    /**
     * Return a String formatted to show the file size in a user friendly format
     *
     *
     * If < 1024 (kb) : size 'bytes'
     * if 1024 < size < 1024^2 : size/1024 kB
     * if 1024^ < size < 1023^3 : size/1024^2 MB
     *
     * @param fileSize Size of the file in bytes
     * @return Formatted string as above
     */
    fun formatFileSize(fileSize: Long): String {
        val unit: String
        val factor: Long
        if (fileSize > UNIT_GB) {
            factor = UNIT_GB
            unit = "GB"
        } else if (fileSize > UNIT_MB) {
            factor = UNIT_MB
            unit = "MB"
        } else if (fileSize > UNIT_KB) {
            factor = UNIT_KB
            unit = "kB"
        } else {
            factor = 1
            unit = "bytes"
        }

        var unitSize = fileSize.toDouble() / factor.toDouble()
        unitSize = round(unitSize * 100) / 100.0
        return "$unitSize $unit"
    }

    fun formatFileSizeMb(fileSize: Long) : String {
        val fileSizeMb = (fileSize.toFloat() / (1024*1024)).roundTo(2)
        return "$fileSizeMb MB"
    }

    fun stripExtensionIfPresent(uri: String): String {
        val lastSlashPos = uri.lastIndexOf('/')
        val lastDotPos = uri.lastIndexOf('.')
        return if (lastDotPos != -1 && lastDotPos > lastSlashPos) {
            uri.substring(0, lastDotPos)
        } else {
            uri
        }
    }



    fun clearTopFromReferrerPath(viewname: String, args: Map<String, String?>, referrerPath: String): String {
        val lastIndex = referrerPath.lastIndexOf("/$viewname?")
        return if (lastIndex != -1) {
            referrerPath.substring(0, referrerPath.indexOf("/", lastIndex))
        } else {
            "/" + viewname + "?" + mapToQueryString(args)
        }
    }

    fun getContentType(filePath: String): ContentType {
        val extension = filePath.substring(filePath.lastIndexOf(".")).lowercase()
        val extensionMap = mapOf(
                ".html" to ContentType.Text.Html,
                ".xml" to ContentType.Text.Xml,
                ".css" to ContentType.Text.CSS,
                ".js" to ContentType.Text.JavaScript,
                ".json" to ContentType.Application.Json,
                ".txt" to ContentType.Text.Html,
                ".xhtml" to ContentType.Text.Html,
                ".jpg" to ContentType.Image.JPEG,
                ".png" to ContentType.Image.PNG,
                ".gif" to ContentType.Image.GIF,
                ".webp" to ContentType("image", "webp"),
                ".mp4" to ContentType.Video.MP4,
                ".mpeg" to ContentType.Video.MPEG,
                ".m4v" to ContentType("video", "x-m4v"),
                ".webm" to ContentType("video", "webm"),
                ".ogv" to ContentType("video", "ogg"),
                ".mp3" to ContentType.Audio.MPEG,
                ".ogg" to ContentType("audio", "ogg"),
        )
        return extensionMap[extension] ?: ContentType.Any
    }

}
