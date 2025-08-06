package com.ustadmobile.libcache.distributed.http

import com.ustadmobile.ihttp.request.iRequestBuilder
import com.ustadmobile.libcache.UstadCache
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.net.URLEncoder
import kotlin.test.Test

class DistributedCacheHttpEndpointTest {

    @Test
    fun givenRequest_willForwardToCache() {
        val cache = mock<UstadCache> { }
        val originalUrl = "http://example.org/image.jpg"
        val request = iRequestBuilder(url = "http://localhost:4242/dcache?url=${URLEncoder.encode(originalUrl, "UTF-8")}")

        val dCacheEndpoint = DistributedCacheHttpEndpoint(cache)
        dCacheEndpoint(request)
        verify(cache).retrieve( argWhere { it.url == originalUrl } )
    }

}