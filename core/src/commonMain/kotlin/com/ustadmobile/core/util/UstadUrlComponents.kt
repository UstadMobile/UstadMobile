package com.ustadmobile.core.util

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
    val endpoint: String,
    val viewName: String,
    val queryString: String
) {

    val arguments: Map<String, String> by lazy(LazyThreadSafetyMode.NONE) {
        val parsedParams = UMFileUtil.parseParams(queryString, '&')

        parsedParams.map {
            UMURLEncoder.decodeUTF8(it.key) to UMURLEncoder.decodeUTF8(it.value)
        }.toMap()
    }

    companion object {

        private const val DIVIDER = "/#/"

        fun parse(url: String) : UstadUrlComponents {
            val dividerIndex = url.indexOf("/#/")
            if(dividerIndex == -1)
                throw IllegalArgumentException("Not a valid UstadUrl: $url")

            //Endpoint should include the trailing /
            val endpoint = url.substring(0, dividerIndex + 1).removeSuffix("umapp/")
            val queryIndex = url.indexOf("?", startIndex = dividerIndex)
            val viewName: String
            val args: String
            if(queryIndex == -1 || queryIndex == (url.length -1)) {
                viewName = url.substring(dividerIndex + DIVIDER.length).removeSuffix("?")
                args = ""
            }else {
                viewName = url.substring(dividerIndex + DIVIDER.length, queryIndex)
                args = url.substring(queryIndex + 1)
            }

            return UstadUrlComponents(endpoint, viewName, args)
        }
    }

}