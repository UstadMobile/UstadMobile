package com.ustadmobile.ihttp.request

import com.ustadmobile.ihttp.headers.IHttpHeaders

interface IHttpRequest {

    val headers: IHttpHeaders

    val url: String

    val method: Method

    fun queryParam(name: String): String?

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