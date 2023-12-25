package com.ustadmobile.lib.rest

import com.ustadmobile.lib.rest.ext.respondCacheResponse
import com.ustadmobile.lib.rest.ext.clientUrl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.request.requestBuilder
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get


fun Route.ContentEntryVersionRoute(
    cache: UstadCache
){
    get("{...}") {
        val url = call.request.clientUrl()
        val cacheResponse = cache.retrieve(requestBuilder(url))
        call.respondCacheResponse(cacheResponse)
    }
}
