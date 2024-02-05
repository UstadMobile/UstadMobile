package com.ustadmobile.libcache.request

import com.ustadmobile.libcache.headers.HttpHeaders

interface HttpRequest {

    val headers: HttpHeaders

    val url: String

    val method: Method

    companion object {

        /**
         * @param methodName because name itself can be obfuscated
         */
        enum class Method(val methodName: String) {
            GET("get"),
            PUT("put"),
            POST("post"),
            HEAD("head"),
            OPTIONS("options"),
            DELETE("delete");


            companion object {

                /**
                 * .Entries does not work after applying proguard
                 */
                private val ALL_METHODS = listOf(GET, PUT, POST, HEAD, OPTIONS, DELETE)
                fun forName(methodName: String): Method {
                    return ALL_METHODS.first { it.methodName.equals(methodName, true) }
                }
            }
        }

    }

}