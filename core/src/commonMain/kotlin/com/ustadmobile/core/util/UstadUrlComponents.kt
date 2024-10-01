package com.ustadmobile.core.util

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import io.ktor.http.*

/**
 * Ustad's URL system is structured as follows:
 *
 * http(s)://server.name[:port]/[subpath/]umapp/[index.html]#/ViewName?argname=argvalue
 *
 * Production mode:
 *   http(s)://server.name[:port]/[subpath/]umapp/#/ViewName?argname1=argvalue1&...
 *
 * Javascript Development Mode:
 *   http://localhost[:port]/#/ViewName?argname1=argvalue1&...
 */
data class UstadUrlComponents(
    val learningSpace: String,
    val viewName: String,
    val queryString: String,
) {

    val arguments: Map<String, String> by lazy(LazyThreadSafetyMode.NONE) {
        val parsedParams = UMFileUtil.parseParams(queryString, '&')

        parsedParams.map {
            UMURLEncoder.decodeUTF8(it.key) to UMURLEncoder.decodeUTF8(it.value)
        }.toMap()
    }

    val viewUri: String by lazy {
        if(queryString.isEmpty()) {
            viewName
        }else {
            "$viewName?$queryString"
        }
    }

    fun fullUrl(divider: String = UstadMobileSystemCommon.LINK_ENDPOINT_VIEWNAME_DIVIDER): String {
        return UMFileUtil.joinPaths(learningSpace, divider, viewUri)
    }

    companion object {

        const val DEFAULT_DIVIDER = "/#/"

        fun parse(url: String, divider: String = DEFAULT_DIVIDER) : UstadUrlComponents {
            val dividerIndex = url.indexOf(divider)
            if(dividerIndex == -1)
                throw IllegalArgumentException("Not a valid UstadUrl: $url")

            //Endpoint should include the trailing /
            val endpoint = url.substring(0, dividerIndex + 1).removeSuffix("umapp/")
            val queryIndex = url.indexOf("?", startIndex = dividerIndex)
            val viewName: String
            val queryString: String
            if(queryIndex == -1 || queryIndex == (url.length -1)) {
                viewName = url.substring(dividerIndex + divider.length).removeSuffix("?")
                queryString = ""
            }else {
                viewName = url.substring(dividerIndex + divider.length, queryIndex)
                queryString = url.substring(queryIndex + 1)
            }

            return UstadUrlComponents(endpoint, viewName, queryString)
        }
    }

}