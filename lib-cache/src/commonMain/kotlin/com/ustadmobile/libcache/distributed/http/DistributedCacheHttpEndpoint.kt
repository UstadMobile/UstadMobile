package com.ustadmobile.libcache.distributed.http

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.response.IHttpResponse
import com.ustadmobile.ihttp.response.StringResponse
import com.ustadmobile.libcache.UstadCache
import java.net.URLDecoder

/**
 * Distributed cache http endpoint ... used when another node wants to retrieve from this node.
 */
class DistributedCacheHttpEndpoint(
    private val cache: UstadCache,
) {

    class DCacheRequest(val originalRequest: IHttpRequest) : IHttpRequest {
        override val headers: IHttpHeaders
            get() = originalRequest.headers

        override val url: String
            get() = originalRequest.queryParam("url") ?: throw IllegalArgumentException("DCache request has no url parameter")

        override val method: IHttpRequest.Companion.Method
            get() = originalRequest.method

        private val queryParamMap: Map<String, String> by lazy {
            url.substringAfter("?", "").split("&").map {
                val split = it.split("=", limit = 2)
                val paramName = URLDecoder.decode(split.first(), "UTF-8")
                val paramVal = split.getOrNull(1)?.let {
                    URLDecoder.decode(it, "UTF-8")
                } ?: ""

                Pair(paramName, paramVal)
            }.toMap()
        }

        override fun queryParam(name: String): String? {
            return queryParamMap[name]
        }
    }

    operator fun invoke(request: IHttpRequest): IHttpResponse {
        if(request.method != IHttpRequest.Companion.Method.GET)
            return StringResponse(
                request, "text/plain", responseCode = 405, body = "Method not allowed"
            )

        return cache.retrieve(DCacheRequest(request))
            ?: StringResponse(request, "text/plain", responseCode = 404, body = "not found")
    }

}