package com.ustadmobile.core.contentformats.har

class RecorderInterceptor : HarInterceptor() {

    override fun intercept(request: HarRequest, jsonArgs: String?): HarEntry {
       return HarEntry()
    }
}