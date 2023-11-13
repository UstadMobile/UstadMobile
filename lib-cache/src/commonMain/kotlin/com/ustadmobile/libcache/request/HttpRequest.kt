package com.ustadmobile.libcache.request

import com.ustadmobile.libcache.headers.HttpHeaders

interface HttpRequest {

    val headers: HttpHeaders

    val url: String

    val method: Method

    companion object {

        enum class Method {
            GET, PUT, POST,
        }

    }

}