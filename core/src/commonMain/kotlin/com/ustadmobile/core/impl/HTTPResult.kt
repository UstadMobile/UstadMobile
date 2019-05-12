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
package com.ustadmobile.core.impl

import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.util.Base64Coder
import java.util.*

/**
 *
 * @author mike
 */
class HTTPResult {

    var response: ByteArray? = null
        private set

    var status: Int = 0
        private set

    /**
     * Provides all responses headers (with the header itself in **lower case**
     * in a hashtable
     * @return
     */
    var responseHeaders: Map<String, String>? = null
        private set

    /**
     * Get a list of all the HTTP headers that have been provided for this
     * request
     *
     * @return String array of available http headers
     */
    val httpHeaderKeys: Array<String?>
        get() {
            val e = responseHeaders!!.keys.iterator()
            val headerKeys = arrayOfNulls<String>(responseHeaders!!.size)
            var index = 0

            while (e.hasNext()) {
                headerKeys[index] = e.next()
                index++
            }

            return headerKeys
        }

    /**
     * Return the size of this http request as per content-length.  This can
     * be used in combination with the HEAD request method to request the content
     * length without actually downloading the content itself
     *
     * @see HTTPResult.HTTP_SIZE_IO_EXCEPTION
     *
     * @see HTTPResult.HTTP_SIZE_NOT_GIVEN
     *
     *
     * @return The content length in bytes if successful, an error flag < 0 otherwise
     */
    val contentLength: Int
        get() {
            var retVal = HTTP_SIZE_NOT_GIVEN
            val contentLengthStr = getHeaderValue("content-length")
            if (contentLengthStr != null) {
                retVal = (contentLengthStr).toInt()
            }
            return retVal
        }

    /**
     *
     * @param response The byte response data from the server
     * @param status the response code returned by the server
     * @param responseHeaders the headers returned by the server in a hashtable (all keys lower case)
     */
    constructor(response: ByteArray, status: Int, responseHeaders: Hashtable<String,String>?) {
        this.response = response
        this.status = status

        //put all headers into lower case to make them case insensitive
        if (responseHeaders != null) {
            this.responseHeaders = mutableMapOf()
            var headerName: String
            val keys = responseHeaders.keys()
            while (keys.hasMoreElements()) {
                headerName = keys.nextElement() as String
                responseHeaders[headerName.toLowerCase()] = responseHeaders[headerName]
            }
        }
    }

    /**
     * Make a result object based on a data URL with the bytes in the response
     * byte array
     *
     * As per: https://en.wikipedia.org/wiki/Data_URI_scheme
     *
     * @param dataURL
     */
    constructor(dataURL: String) {
        val dataStarts = dataURL.indexOf(',')

        responseHeaders = null
        var isBase64 = false

        if (dataStarts > DATA_URI_PREFIX.length) {
            val infoSection = dataURL.substring(DATA_URI_PREFIX.length,
                    dataStarts)
            val params = UMFileUtil.parseParams(infoSection, ';')
            val keys = params.keys.iterator()
            var paramName: String

            var charset: String? = null
            var mediaType: String? = null
            while (keys.hasNext()) {
                paramName = keys.next()
                if (paramName == "charset") {
                    charset = params[paramName] as String
                } else if (paramName == "base64") {
                    isBase64 = true
                } else {
                    //it must be the media type
                    mediaType = paramName
                }
            }

            if (mediaType != null) {
                responseHeaders = mutableMapOf();
                if (charset != null) {
                    mediaType += ";charset=$charset"
                }
                (responseHeaders as MutableMap<String, String>)["content-type"] = mediaType
            }
        }

        if (isBase64) {
            val offset = dataStarts + 1
            val charArr = CharArray(dataURL.length - offset)
            var c: Char
            var p = 0
            for (i in offset until dataURL.length) {
                c = dataURL[i]
                if (c != ' ' && c != '\r' && c != '\n' && c != '\t') {
                    charArr[p++] = c
                }
            }
            response = Base64Coder.decodeToByteArray(charArr.toString())
            status = 200
        }

    }

    /**
     * Get the suggested filename for this HTTP Request: if the HTTP request
     * has a content-disposition header we will use it to provide the filename;
     * otherwise we will use the filename portion of the URL
     *
     * @param url The entire URL
     *
     * @return Filename suggested by content-disposition if any; otherwise the filename portion of the URL
     */
    fun getSuggestedFilename(url: String): String {
        var suggestedFilename: String? = null

        if (responseHeaders != null && responseHeaders!!.containsKey("content-disposition")) {
            val dispositionHeaderStr = responseHeaders!!["content-disposition"]
            val dispositionHeader = UMFileUtil.parseTypeWithParamHeader(dispositionHeaderStr as String)
            if (dispositionHeader.params != null && dispositionHeader.params!!.containsKey("filename")) {
                suggestedFilename = UMFileUtil.filterFilename(
                        dispositionHeader.params!!["filename"] as String)
            }
        }


        if (suggestedFilename == null) {
            suggestedFilename = UMFileUtil.getFilename(url)
        }

        return suggestedFilename
    }


    /**
     *
     * @param key
     * @return
     */
    fun getHeaderValue(key: String): String? {
        val valObj = responseHeaders!![key.toLowerCase()]
        return valObj?.toString()
    }

    companion object {

        val GET = "GET"


        val HTTP_SIZE_NOT_GIVEN = -1

        val HTTP_SIZE_IO_EXCEPTION = -2

        val DATA_URI_PREFIX = "data:"
    }

}
