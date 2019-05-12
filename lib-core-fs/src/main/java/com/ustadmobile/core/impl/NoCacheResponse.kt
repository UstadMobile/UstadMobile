package com.ustadmobile.core.impl

import com.ustadmobile.core.impl.http.UmHttpResponse

import java.io.InputStream

class NoCacheResponse(private val networkResponse: UmHttpResponse) : AbstractCacheResponse() {

    override val fileUri: String?
        get() = null

    override val isFresh: Boolean
        get() = true

    override val responseBody: ByteArray?
        get() = networkResponse.responseBody

    override val responseAsStream: InputStream?
        get() = networkResponse.responseAsStream

    override val isSuccessful: Boolean
        get() = networkResponse.isSuccessful

    override val status: Int
        get() = networkResponse.status

    init {
        cacheResponse = AbstractCacheResponse.MISS
    }

    override fun isFresh(timeToLive: Int): Boolean {
        return true
    }

    override fun getHeader(headerName: String): String? {
        return networkResponse.getHeader(headerName)
    }
}
