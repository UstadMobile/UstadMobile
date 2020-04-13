package com.ustadmobile.core.contentformats.har

abstract class HarInterceptor {

    abstract fun intercept(request: HarRequest, jsonArgs: String?): HarEntry

    val interceptorMap = mapOf<String, HarInterceptor>(
            "RecorderInterceptor" to RecorderInterceptor()
    )
}