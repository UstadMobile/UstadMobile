package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.ihttp.headers.IHttpHeaders

class ResponseValidityChecker {

    /**
     * Check if headers1 matches headers2 as per the etag and last modified date.
     */
    fun isMatchingEtagOrLastModified(headers1: IHttpHeaders, headers2: IHttpHeaders) : Boolean {
        val headers1Etag = headers1["etag"]
        val headers2Etag = headers2["etag"]
        if(headers1Etag != null && headers1Etag == headers2Etag) {
            return true
        }

        val headers1LastModified = headers1["last-modified"]
        val headers2LastModified = headers2["last-modified"]
        return headers1LastModified != null && headers1LastModified == headers2LastModified
    }

}