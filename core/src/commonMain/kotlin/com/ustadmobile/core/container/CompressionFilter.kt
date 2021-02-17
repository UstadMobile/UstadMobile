package com.ustadmobile.core.container

interface CompressionFilter {

    fun shouldCompress(uri: String, mimeType: String?): Boolean

}